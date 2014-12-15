/*******************************************************************************
DfromW.java - Naive matrix-vector multiplication in hadoop.
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
Description: DfromW is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

public class DfromW extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		private final IntWritable from_node_int = new IntWritable();
		int makesym = 0;

		public void configure(JobConf job) {
			makesym = Integer.parseInt(job.get("makesym"));

			String input_file = job.get("map.input.file");

			System.out.println("DfromW.MapPass1: makesym = " + makesym);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			// edge : ROWID		COLID
			output.collect( new IntWritable(Integer.parseInt(line[1])), new Text(line[0]) );
		}
	}

    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, DoubleWritable>
    {
		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;

			while (values.hasNext()) {
				values.next();

				sum += 1;
			}

			output.collect( key, new DoubleWritable( sum ) );
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
    protected Path tempmv_path = null;
	protected Path output_path = null;
	protected int nreducer = 1;
	int makesym = 0;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new DfromW(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("DfromW <edge_path> <tempmv_path> <output_path> <# of nodes> <# of reducers> <makesym or nosym>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);
		output_path = new Path(args[1]);				
		nreducer = Integer.parseInt(args[2]);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(output_path);

		System.out.println("Running DfromW... edge_path=" + args[0] + ", out_path=" + args[1] );
		JobClient.runJob(configPass1());
		System.out.println("DfromW done. Output is saved in HDFS " + args[1] );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DfromW.class);
		conf.set("makesym", "" + makesym);

		conf.setJobName("DfromW_pass1");
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);

		FileInputFormat.setInputPaths(conf, edge_path);  
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);
		conf.setMapOutputValueClass(Text.class);

		return conf;
    }

}

