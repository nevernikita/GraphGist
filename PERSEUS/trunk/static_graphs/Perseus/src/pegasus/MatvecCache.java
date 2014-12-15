/*******************************************************************************
MatvecCache.java - Naive matrix-vector multiplication in hadoop.
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
Description: MatvecCache is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

public class MatvecCache extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		int makesym = 0;
		boolean isLeftmat = false;
		int col = 0;

		Matrix RV = null;
		int rv_row = 0;


		// read right vector from string encoding.
		// The format of the string is #ROW		elem(1)  elem(2) ... 
		protected Matrix read_right_vector(String vec_str) {
			int i;
			String [] tokens = vec_str.split("\t");
			
			rv_row = Integer.parseInt(tokens[0]);

			System.out.println("Making right vector of size " + rv_row );
			Matrix right_matrix = new Matrix(rv_row, 1);

			for(i = 1; i < tokens.length; i++) {
				double cur_val = Double.parseDouble(tokens[i]);

				int elem_seq_index = i-1;
				int cur_row_index = elem_seq_index;

				//System.out.println("[DEBUG] row=" + cur_row_index + ", col=" + cur_col_index);
				
				right_matrix.set(cur_row_index, 0, cur_val);
			}

			return right_matrix;
		}

		public void configure(JobConf job) {
			//makesym = Integer.parseInt(job.get("makesym"));
			//String left_filename = job.get("left_filename");
			//String right_filename = job.get("right_filename");
			String input_file = job.get("map.input.file");
			String right_vec_str = job.get("right_vector");

//			int lzq_index = input_file.indexOf("lz_q");
//			int slash_index = input_file.indexOf("/", lzq_index);
//			col = Integer.parseInt( input_file.substring(lzq_index + 4, slash_index) ) - 1;

			System.out.println("Loading right matrix...");
			RV = read_right_vector( right_vec_str );
			System.out.println("Done.");

			System.out.println("MatvecCache.MapPass1: makesym=" + makesym + ", right_vector=" + right_vec_str + ", col=" + col);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			// the format is ROW VALUE

			if(line.length == 3 ) {	// edge
				int dstid = Integer.parseInt(line[1]);

				double out_val = 0;
				if( line[2].charAt(0) == 'v' )
					out_val = Double.parseDouble(line[2].substring(1));
				else
					out_val = Double.parseDouble(line[2]);

				out_val *= (RV.get(dstid, 0));

				output.collect( new IntWritable(Integer.parseInt(line[0])), new DoubleWritable(out_val) );
			}
			
		}
	}

    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, Text, DoubleWritable>
    {
		Matrix RV = null;
		int rv_row = 0;
		//int rm_col = 0;
		int makesym = 0;

		// read right vector from string encoding.
		// The format of the string is #ROW		elem(1)  elem(2) ... 
		protected Matrix read_right_vector(String vec_str) {
			int i;
			String [] tokens = vec_str.split("\t");
			
			rv_row = Integer.parseInt(tokens[0]);

			System.out.println("Making right vector of size " + rv_row );
			Matrix right_matrix = new Matrix(rv_row, 1);

			for(i = 1; i < tokens.length; i++) {
				double cur_val = Double.parseDouble(tokens[i]);

				int elem_seq_index = i-1;
				int cur_row_index = elem_seq_index;

				//System.out.println("[DEBUG] row=" + cur_row_index + ", col=" + cur_col_index);
				
				right_matrix.set(cur_row_index, 0, cur_val);
			}

			return right_matrix;
		}

		public void configure(JobConf job) {
			String right_vec_str = job.get("right_vector");

			System.out.println("MatvecCache.RedPass1: makesym=" + makesym + ", right_vector=" + right_vec_str);

			System.out.println("Loading right matrix...");
			RV = read_right_vector( right_vec_str );
			System.out.println("Done.");
		}


		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			//int i;
			//float vector_val = 0;

			// the input is key: colid
			//              val: rowid   value
			int left_colid = key.get();

			while (values.hasNext()) {
				String line_text = values.next().toString();
				final String[] tokens = line_text.split("\t");

				int left_rowid = Integer.parseInt(tokens[0]);
				double cur_val = Double.parseDouble(tokens[1]);

				String out_key = "" + left_rowid;
				double out_val = cur_val * RV.get(left_colid, 0);

				output.collect( new Text(out_key), new DoubleWritable(out_val) );
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
    protected Path tempmm_path = new Path("vav_tempmv_path");
	protected Path output_path = new Path("matvec_out_path");
	protected int number_nodes = 0;
	protected int nreducers = 1;
	protected String edge_file_name;
	protected String job_name_base;
	protected int lz_q_no = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MatvecCache(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MatvecCache <left edge_path> <# of reducers> <right edge file> <m> <n>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);
		nreducers = Integer.parseInt(args[1]);
		String right_vec_file = args[2];
		int right_vec_length = Integer.parseInt(args[3]);


		// read from the right edge file 
		// load matrix_file into Tm
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( right_vec_file ), "UTF8"));
		String right_vec_str = "" + right_vec_length;

		String cur_str = "";
		while(cur_str != null) {
			cur_str = in.readLine();
			if(cur_str == null )
				break;

			right_vec_str += "\t" + cur_str;
		}

		System.out.println("Running MatvecCache...");
		JobClient.runJob( configPass1(right_vec_str) );
		//JobClient.runJob(configPass2());
		System.out.println("MatvecCache Done. Output is saved in HDFS " + output_path.getName() );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 (String in_right_vec_str) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecCache.class);
		conf.set("right_vector", "" + in_right_vec_str);
		conf.setJobName("MatvecCache_pass1_" + job_name_base);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(output_path);


		FileInputFormat.setInputPaths(conf, edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

	// Configure pass2
    protected JobConf configPass2 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecCache.class);

		conf.setJobName("MatvecCache_pass2_" + job_name_base);
		
		conf.setMapperClass(PegasusUtils.MapIdentityDouble.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(output_path);


		FileInputFormat.setInputPaths(conf, tempmm_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}
