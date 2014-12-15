/*******************************************************************************
MatmatNaive.java - Naive matrix-vector multiplication in hadoop.
Copyright (c) 2009 U Kang(ukang@cs.cmu.edu)
All rights preserved

You may use this code without fee, for educational or research purposes.
You agree to cite the following paper whenever you use the code:
 *** John Tomson and Tom Johson: 
 *** "Graph mining for fun and profit", 
 *** Proc. ACM KDD, Las Vegas, Aug. 2008 

Any for-profit use requires the consent of the author(s).




Version: 0.10
Last modified: Mar 22, 2009
Description: MatmatNaive is a hadoop program which performs matrix-vector multiplication using element-wise join.

How to run: Use run_mvnaive.sh to run the program in hadoop.	
	Usage) ./run_mvnaive.sh [input HDFS dir] [output HDFS dir] [#_of_rows] [#_of_machines_in_hadoop] [edge_file] [1 or 0: copy_edgefile_or_not]

	(Arguments)
	[input HDFS dir]: The HDFS directory where input matrix or vectors are saved.
	[output HDFS dir]: The HDFS directory where where result is saved.
	[#_of_rows]: Number of rows(or columns) in the input matrix( or vector). We assume the matrix with same number of rows and columns.
	[#_of_machines_in_hadoop]: number of machines to use in hadoop.
	[edge_file]: input matrix or vector file name. This name is used only for deciding log file name.
	[1 or 0: copy_edgefile_or_not]: If the input edge or vector file is in the local disk, set to 1. Then the local file will be copied to the input HDFS directory. If  the input edge or vector file is already in the input HDFS directory, set to 0.

Example: ./run_mvnaive.sh mv_input mv_output 5 2 3 mv_input_5x5.txt 1
*******************************************************************************/

package pegasus;

import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class MatmatNaive extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		int makesym = 0;
		boolean isLeftmat = false;

		public void configure(JobConf job) {
			String left_filename = job.get("left_edge");
			String right_filename = job.get("right_edge");
			String input_file = job.get("map.input.file");

			if( input_file.contains(left_filename) )
				isLeftmat = true;

			System.out.println("MatmatNaive.MapPass1: makesym=" + makesym + ", isLeftmat=" + isLeftmat + ", input_file=" + input_file + ", left_filename=" + left_filename);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			// the format is ROW COL VALUE
			
			if(isLeftmat) {	// if left matrix, then output key is column
				output.collect(new IntWritable(Integer.parseInt(line[1])), new Text("L" + line[0] + "\t" + line[2]));
			} else {	// if right matrix, then output key is row
				output.collect(new IntWritable(Integer.parseInt(line[0])), new Text("R" + line[1] + "\t" + line[2]));
			}
		}
	}



    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, Text, DoubleWritable>
    {
	    ArrayList<Integer> to_nodes_list = new ArrayList<Integer>();
	    ArrayList<Double> to_val_list = new ArrayList<Double>();

		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			int i;
			double vector_val = 0;

			to_nodes_list.clear();
			to_val_list.clear();

	        Map<Integer, Double> left_row_map = new HashMap<Integer, Double>();
	        Map<Integer, Double> right_col_map = new HashMap<Integer, Double>();

			while (values.hasNext()) {
				String line_text = values.next().toString();

				//System.out.println("line_text=[" + line_text + "]");
				final String[] line = line_text.split("\t");

				if( line[0].charAt(0) == 'L' ) {
					left_row_map.put(Integer.parseInt(line[0].substring(1)), Double.parseDouble( line[1] ) );
				} else {
					right_col_map.put(Integer.parseInt(line[0].substring(1)), Double.parseDouble( line[1] ) );
				}
			}

			Iterator<Map.Entry<Integer, Double>> iter_left_row = left_row_map.entrySet().iterator();
			while(iter_left_row.hasNext()){
				Map.Entry<Integer, Double> left_ent = iter_left_row.next();

				Iterator<Map.Entry<Integer, Double>> iter_right_col = right_col_map.entrySet().iterator();
				while(iter_right_col.hasNext()){
					Map.Entry<Integer, Double> right_ent = iter_right_col.next();

					String out_key = "" + left_ent.getKey() + "\t" + right_ent.getKey();
					//System.out.println("out_key=" + out_key);
					
					output.collect( new Text(out_key), new DoubleWritable( left_ent.getValue() * right_ent.getValue() ) );
				}
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // PASS 2: merge partial multiplication results
    //////////////////////////////////////////////////////////////////////
	public static class MapPass2 extends MapReduceBase implements Mapper<LongWritable, Text, Text, DoubleWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			String [] tokens = line_text.split("\t");

			output.collect( new Text(tokens[0] + "\t" + tokens[1]) , new DoubleWritable(Double.parseDouble(tokens[2])) );
		}
	}


    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path left_edge_path = null;
    protected Path right_edge_path = null;
    protected Path tempmv_path = null;
	protected Path output_path = null;
	protected int number_nodes = 0;
	protected int ntask = 1;
	protected String edge_file_name;
	protected String job_name_base;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MatmatNaive(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MatmatNaive <edge_path> <curmv_path> <tempmv_path> <nextmv_path> <output_path> <# of nodes> <# of replication> <# of tasks> <edge_file> <same or diff> <inv or reg>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	protected BufferedWriter open_log_file(String job_name_base) throws Exception
	{
		FileWriter fstream = new FileWriter(job_name_base + ".log");
		BufferedWriter out = new BufferedWriter(fstream);

		return out;
	}

	protected String get_cur_datetime()
	{
		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	protected String format_duration(long millis)
	{
		String DATE_FORMAT_NOW = "HH:mm:ss";

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(millis));
	}

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		left_edge_path = new Path(args[0]);
		right_edge_path = new Path(args[1]);
		tempmv_path = new Path("matmatnaive_temp");
		output_path = new Path(args[2]);				
		//number_nodes = Integer.parseInt(args[3]);
		ntask = Integer.parseInt(args[3]);

		System.out.println("Running MatmatNaive...");
		JobClient.runJob(configPass1());
		JobClient.runJob(configPass2());
		System.out.println("Done MatmatNaive.");

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatmatNaive.class);
		//conf.set("number_nodes", "" + number_nodes);
		conf.set("right_edge", "" + right_edge_path.getName());
		conf.set("left_edge", "" + left_edge_path.getName());
		conf.setJobName("MatvecNaive_pass1_" + job_name_base);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);


		final FileSystem fs = FileSystem.get(conf);
		fs.delete(tempmv_path);


		FileInputFormat.setInputPaths(conf, left_edge_path, right_edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, tempmv_path);  

		conf.setNumReduceTasks( ntask );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(DoubleWritable.class);
		conf.setMapOutputKeyClass(IntWritable.class);
		conf.setMapOutputValueClass(Text.class);



		return conf;
    }

	// Configure pass2
    protected JobConf configPass2 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatmatNaive.class);
		//conf.set("number_nodes", "" + number_nodes);

		conf.setJobName("MatmatNaive_pass2_" + job_name_base);
		
		conf.setMapperClass(MapPass2.class);        
		conf.setReducerClass(PegasusUtils.RedSumDoubleTextKey.class);
		conf.setCombinerClass(PegasusUtils.RedSumDoubleTextKey.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(output_path);


		FileInputFormat.setInputPaths(conf, tempmv_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( ntask );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

}

