/*******************************************************************************
CombinePat.java - Naive matrix-vector multiplication in hadoop.
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
Description: CombinePat is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

// D^{-1/2} W D^{-1/2}
// (Left multiply) W D^{-1/2} : row-normalize
// (Right multiply) W D^{-1/2} : column-normalize

public class CombinePat extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
/*
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		private final IntWritable from_node_int = new IntWritable();
		int makesym = 0;
		String dpos = null;
		boolean isDpath = false;

		public void configure(JobConf job) {
			String input_file = job.get("map.input.file");

			if(input_file.contains("nw_dpath"))
				isDpath = true;

			System.out.println("CombinePat.MapPass1: isDpath = " + isDpath+ ",dpos=" + dpos);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			if( line[0].charAt(0) == 'v') {
		

			//if( line.length == 2 ) {	// vector : ROWID	VALUE('vNNNN')
			if( isDpath ) {	// vector : ROWID	VALUE('vNNNN')
				from_node_int.set( Integer.parseInt(line[0]) );
				output.collect( from_node_int, new Text("v" + line[1]) );
			} else {					// edge : ROWID		COLID
				if( line.length == 2 ) {
					if(dpos.charAt(0) == 'l' )	// left D : row-normalize
						output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1]) );
					else						// right D : column-normzlize
						output.collect( new IntWritable(Integer.parseInt(line[1])), new Text(line[0]) );
				} else if( line.length == 3 ) {
					if(dpos.charAt(0) == 'l' )	// left D : row-normalize
						output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1] + "\t" + line[2]) );
					else						// right D : column-normzlize
						output.collect( new IntWritable(Integer.parseInt(line[1])), new Text(line[0] + "\t" + line[2]) );
				}
			}
		}
	}
*/

    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, Text>
    {

		public void configure(JobConf job) {
			String input_file = job.get("map.input.file");

			System.out.println("CombinePat.RedPass1");
			System.out.println("input_file = " + input_file);
		}

		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
        {
	        int category = 0;
			int sign_val = 0;

			while (values.hasNext()) {
				String line_text = values.next().toString();

				if(line_text.charAt(0) == 'v') {	// ritz vector
					if(line_text.charAt(1) == '-') 
						sign_val = 2;
					else
						sign_val = 1;
				} else {		// category vector
					category = Integer.parseInt(line_text);
				}
			}

			if( category > 0 && sign_val > 0 ) {
				int out_val = 10*category + sign_val;
				output.collect(new IntWritable(out_val), new Text( "1" ) );
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected int number_nodes = 0;
	protected int nreducer = 1;
	protected String edge_file_name;
	protected String job_name_base;
	int makesym = 0;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new CombinePat(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("CombinePat <edge_path> <tempmv_path> <output_path> <# of nodes> <# of reducers> <makesym or nosym>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		Path ritz_vector_path = new Path(args[0]);
		Path category_path = new Path(args[1]);
		Path output_path = new Path(args[2]);				
		nreducer = Integer.parseInt(args[3]);

		System.out.println("Running CombinePat... ritz_vector_path=" + args[0] + ",cat_path-" + category_path + ", out_path=" + args[2] + ", nreducer=" + nreducer);

		Path temp_cp_path = new Path("temp_cp_path");

		JobClient.runJob(configPass1(ritz_vector_path, category_path, temp_cp_path, nreducer));
		JobClient.runJob(configPass2(temp_cp_path, output_path, nreducer));

		System.out.println("CombinePat done. Output is in HDFS " + output_path.getName() );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 (Path ritz_vector_path, Path category_path, Path out_path, int nreducer) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), CombinePat.class);

		conf.setJobName("CombinePat");
		
		conf.setMapperClass(PegasusUtils.MapIdentity.class);        
		conf.setReducerClass(RedPass1.class);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(out_path);

		FileInputFormat.setInputPaths(conf, ritz_vector_path, category_path);  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }

    protected JobConf configPass2 (Path in_path, Path out_path, int nreducer) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), CombinePat.class);

		conf.setJobName("CombinePat 2");
		
		conf.setMapperClass(PegasusUtils.MapIdentityDouble.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(out_path);

		FileInputFormat.setInputPaths(conf, in_path);  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( 1 );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

}

