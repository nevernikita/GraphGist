/*******************************************************************************
VecarrmatCache.java - Naive matrix-vector multiplication in hadoop.
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
Description: VecarrmatCache is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

import Jama.*; 

import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class VecarrmatCache extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, DoubleWritable>
    {
		int makesym = 0;
		boolean isLeftmat = false;
		int col = 0;
		Matrix RM = null;
		int rm_row = 0;
		int rm_col = 0;

		protected Matrix read_right_matrix(String mat_str) {
			int i;
			String [] tokens = mat_str.split("\t");
			
			rm_row = Integer.parseInt(tokens[0]);
			rm_col = Integer.parseInt(tokens[1]);

			System.out.println("Making rightmatrix of " + rm_row + " by " + rm_col);
			Matrix right_matrix = new Matrix(rm_row, rm_col);

			for(i = 2; i < tokens.length; i++) {
				double cur_val = Double.parseDouble(tokens[i]);

				int elem_seq_index = i-2;
				int cur_row_index = (elem_seq_index / rm_col);
				int cur_col_index = (elem_seq_index % rm_col);

				//System.out.println("[DEBUG] row=" + cur_row_index + ", col=" + cur_col_index);
				
				right_matrix.set(cur_row_index, cur_col_index, cur_val);
			}

			return right_matrix;
		}

		public void configure(JobConf job) {
			//makesym = Integer.parseInt(job.get("makesym"));
			//String left_filename = job.get("left_filename");
			//String right_filename = job.get("right_filename");
			String input_file = job.get("map.input.file");
			String right_mat_str = job.get("right_matrix");

			int lzq_index = input_file.indexOf("lz_q");
			int slash_index = input_file.indexOf("/", lzq_index);
			col = Integer.parseInt( input_file.substring(lzq_index + 4, slash_index) ) - 1;

			System.out.println("VecarrmatCache.MapPass1: makesym=" + makesym + ", right_matrix=" + right_mat_str + ", col=" + col);
			System.out.println("input_file = " + input_file);

			System.out.println("Loading right matrix...");
			RM = read_right_matrix( right_mat_str );
			System.out.println("Done.");
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			// the format is ROW VALUE

			double cur_val = 0;
			if( line[1].charAt(0) == 'v' )
				cur_val = Double.parseDouble(line[1].substring(1));
			else
				cur_val = Double.parseDouble(line[1]);

			for(int i = 0; i < rm_col; i++) {
				String out_key = "" + line[0] + "\t" + i;

				double out_val = cur_val * RM.get(col, i);

				output.collect( new Text(out_key), new DoubleWritable(out_val) );
			}
		}
	}

    public static class RedPass1 extends MapReduceBase implements Reducer<Text, DoubleWritable, Text, DoubleWritable>
    {
		public void reduce (final Text key, final Iterator<DoubleWritable> values, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;

			while (values.hasNext()) {
				double cur_value = values.next().get();
				sum += cur_value;
			}

			output.collect( key, new DoubleWritable( sum ) );
		}
    }


    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
    //protected Path tempmm_path = new Path("tempmm_path");
	protected Path output_path = new Path("matmat_out_path");
	protected int number_nodes = 0;
	protected int nreducers = 1;
	protected String edge_file_name;
	protected String job_name_base;
	protected int lz_q_no = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new VecarrmatCache(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("VecarrmatCache <left edge_path> <# of reducers> <right edge file> <m> <n>");

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
		if( args.length != 6 ) {
			return printUsage();
		}

		lz_q_no = Integer.parseInt(args[0]);//edge_path = new Path(args[0]);
		nreducers = Integer.parseInt(args[1]);
		String right_edge_file = args[2];
		int right_m = Integer.parseInt(args[3]);
		int right_n = Integer.parseInt(args[4]);
		String q_read_mode = args[5];


		// read from the right edge file 
		// load matrix_file into Tm
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( right_edge_file ), "UTF8"));
		String right_matrix_str = "" + right_m + "\t" + right_n;

		String cur_str = "";
		while(cur_str != null) {
			cur_str = in.readLine();
			if(cur_str == null )
				break;

			right_matrix_str += "\t" + cur_str;
		}

		System.out.println("Running VecarrmatCache...");
		JobClient.runJob( configPass1(right_matrix_str, q_read_mode) );
		//JobClient.runJob(configPass2());
		System.out.println("VecarrmatCache Done. Output is saved in HDFS " + output_path.getName());

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 (String in_right_matrix_str, String q_read_mode) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), VecarrmatCache.class);
		conf.set("right_matrix", "" + in_right_matrix_str);
		conf.setJobName("MatmatCache_pass1_" + job_name_base);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);
		conf.setCombinerClass(RedPass1.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(output_path);

		if( q_read_mode == "upto" ) {
			FileInputFormat.setInputPaths(conf, "lz_q1");  
			for(int i = 2; i <= lz_q_no; i++) 
				FileInputFormat.addInputPaths(conf, ("lz_q" + i));  
		} else {
			// q_read_mode = "exact"NNNN
			int input_path_no = Integer.parseInt(q_read_mode.substring(5));
			FileInputFormat.setInputPaths(conf, "lz_q" + input_path_no);  
		}
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }


}

