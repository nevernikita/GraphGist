/*******************************************************************************
VecarrmatNaiveMM.java - Naive matrix-vector multiplication in hadoop.
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
Description: VecarrmatNaiveMM is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FSDataInputStream;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import org.apache.hadoop.mapred.*;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class VecarrmatNaiveMM extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		private final IntWritable from_node_int = new IntWritable();
		int makesym = 0;
		boolean isRightMat = false;
		int col;

		public void configure(JobConf job) {
			String input_file = job.get("map.input.file");
			String right_mat_name = job.get("right_map_name");

			if( input_file.contains(right_mat_name) )
				isRightMat = true;
			else
				isRightMat = false;

			if( isRightMat == false) {
				int lzq_index = input_file.indexOf("lz_q");
				int slash_index = input_file.indexOf("/", lzq_index);
				col = Integer.parseInt( input_file.substring(lzq_index + 4, slash_index) ) - 1;
			}
	
			System.out.println("VecarrmatNaiveMM.MapPass1: makesym = " + makesym + ", right_mat_name=" + right_mat_name + ", isRightMat = " + isRightMat);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			if( isRightMat == false ) {
				double out_val = 0;
				if( line[1].charAt(0) == 'v' )
					out_val = Double.parseDouble(line[1].substring(1));
				else
					out_val = Double.parseDouble(line[1]);

					output.collect( new IntWritable(col), new Text("L" + line[0] + "\t" + out_val) );
			} else {	// Right Mat
				output.collect( new IntWritable(Integer.parseInt(line[0])), new Text("R" + line[1] + "\t" + line[2]) );
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
    protected Path edge_path = null;
	protected Path right_mat_path = null;
    protected Path tempmv_path = null;
	protected Path output_path = null;
	protected int number_nodes = 0;
	protected int ntask = 1;
	protected String edge_file_name;
	protected String job_name_base;
	int makesym = 0;
	protected int lz_q_no = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new VecarrmatNaiveMM(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("VecarrmatNaiveMM <edge_path> <tempmv_path> <output_path> <# of nodes> <# of reducers> <makesym or nosym>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			return printUsage();
		}

		System.out.println("Running VecarrmatNaiveMM...");

		lz_q_no = Integer.parseInt(args[0]);//edge_path = new Path(args[0]);
		right_mat_path = new Path(args[1]);
		tempmv_path = new Path("vecarrvec_naive_tempmv");
		output_path = new Path("vecarrmat_naive_out");				
		ntask = Integer.parseInt(args[2]);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(tempmv_path);
		fs.delete(output_path);

		JobClient.runJob(configPass1());
		JobClient.runJob(configPass2());

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), VecarrmatNaiveMM.class);
		conf.set("right_map_name", right_mat_path.getName());
		conf.setJobName("VecarrmatNaiveMM_pass1_" + job_name_base);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);

		FileInputFormat.setInputPaths(conf, right_mat_path);  
		for(int i = 1; i <= lz_q_no; i++)  {
			System.out.println("Adding q" + i +" to the input path");
			FileInputFormat.addInputPaths(conf, ("lz_q" + i));  
		}

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
		final JobConf conf = new JobConf(getConf(), VecarrmatNaiveMM.class);

		conf.setJobName("VecarrmatNaiveMM_pass2_" + job_name_base);
		
		conf.setMapperClass(MapPass2.class);        
		conf.setReducerClass(PegasusUtils.RedSumDoubleTextKey.class);
		conf.setCombinerClass(PegasusUtils.RedSumDoubleTextKey.class);

		FileInputFormat.setInputPaths(conf, tempmv_path);  
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( ntask );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

}

