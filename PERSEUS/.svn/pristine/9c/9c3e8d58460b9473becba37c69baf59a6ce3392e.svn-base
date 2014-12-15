/*******************************************************************************
DTPL.java - Naive matrix-vector multiplication in hadoop.
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
Description: DTPL is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

public class DTPL extends Configured implements Tool 
{

    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, DoubleWritable>
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
			//String right_vec_str = job.get("right_vector");

			System.out.println("DTPL.RedPass1: makesym=" + makesym );

			System.out.println("Loading right matrix...");
			//RV = read_right_vector( right_vec_str );
			System.out.println("Done.");
		}


		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			//int i;
			//float vector_val = 0;

			// the input is key: colid
			//              val: rowid   value
			int degree = 0;
			double tri_count = 0;

			while (values.hasNext()) {
				String line_text = values.next().toString();
				if( line_text.charAt(0) == 'D' )
					degree = Integer.parseInt(line_text.substring(1));
				else
					tri_count = Double.parseDouble(line_text);
			}

			output.collect(new IntWritable(degree), new DoubleWritable(tri_count) );
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path tempmm_path = new Path("dtpl_tempmv_path");
	protected int number_nodes = 0;
	protected int nreducers = 1;
	protected String edge_file_name;
	protected String job_name_base;
	protected int lz_q_no = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new DTPL(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("DTPL <left edge_path> <# of reducers> <right edge file> <m> <n>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		Path tri_path = new Path(args[0]);
		Path deg_path = new Path(args[1]);
		Path out_path = new Path(args[2]);
		nreducers = Integer.parseInt(args[3]);

		System.out.println("Running DTPL...");
		JobClient.runJob( configPass1(tri_path, deg_path, nreducers) );
		JobClient.runJob( configPass2(out_path, nreducers) );
		System.out.println("DTPL Done. Output is saved in HDFS " + out_path.getName() );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 (Path tri_path, Path deg_path, int nreducer) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DTPL.class);
		conf.setJobName("DTPL_pass1_" + job_name_base);
		
		conf.setMapperClass(PegasusUtils.MapIdentity.class);        
		conf.setReducerClass(RedPass1.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(tempmm_path);


		FileInputFormat.setInputPaths(conf, tri_path, deg_path);  
		FileOutputFormat.setOutputPath(conf, tempmm_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setMapOutputValueClass(Text.class);
		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

	// Configure pass2
    protected JobConf configPass2 (Path out_path, int nreducer) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DTPL.class);

		conf.setJobName("DTPL_pass2_" + job_name_base);
		
		conf.setMapperClass(PegasusUtils.MapIdentityDouble.class);        
		conf.setReducerClass(PegasusUtils.RedAvgDouble.class);
		conf.setCombinerClass(PegasusUtils.RedAvgDouble.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(out_path);


		FileInputFormat.setInputPaths(conf, tempmm_path);  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}

