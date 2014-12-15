/*******************************************************************************
VecarrvecNaiveMV.java - Naive matrix-vector multiplication in hadoop.
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
Description: VecarrvecNaiveMV is a hadoop program which performs matrix-vector multiplication using element-wise join.

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

public class VecarrvecNaiveMV extends Configured implements Tool 
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

			System.out.println("VecarrvecNaiveMV.MapPass1: makesym = " + makesym);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			if( line.length == 2 ) {	// vector : ROWID	VALUE('vNNNN')
				if( line[1].charAt(0) == 'v' ) {	// vector : ROWID	VALUE('vNNNN')
					from_node_int.set( Integer.parseInt(line[0]) );
					output.collect( from_node_int, new Text(line[1]) );
				} else {					// edge : ROWID		COLID
					output.collect( new IntWritable(Integer.parseInt(line[1])), new Text(line[0]) );
					if(makesym == 1)
						output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1]) );
				}
			} else if(line.length == 3) {					// edge: ROWID    COLID    VALUE
				output.collect( new IntWritable(Integer.parseInt(line[1])), new Text(line[0] + "\t" + line[2]) );
				if(makesym == 1)
					output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1] + "\t" + line[2]) );
			}
		}
	}

    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, DoubleWritable>
    {
	    ArrayList<Integer> to_nodes_list = new ArrayList<Integer>();
	    ArrayList<Double> to_val_list = new ArrayList<Double>();

		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			int i;
			double vector_val = 0;

			to_nodes_list.clear();
			to_val_list.clear();

	        Map<Integer, Double> to_map = new HashMap<Integer, Double>();

			while (values.hasNext()) {
				String line_text = values.next().toString();
				final String[] line = line_text.split("\t");

				if( line.length == 1 ) {	
					if(line_text.charAt(0) == 'v')	// vector : VALUE
						vector_val = Double.parseDouble(line_text.substring(1));
					else {		// edge : ROWID
						to_map.put(Integer.parseInt(line[0]), new Double(1.0) );
					}
				} else {					// edge : ROWID		VALUE
					to_map.put(Integer.parseInt(line[0]), Double.parseDouble( line[1] ) );
				}

/*
				if( line.length == 1 ) {	
					if(line_text.charAt(0) == 'v')	// vector : VALUE
						vector_val = Double.parseDouble(line_text.substring(1));
					else {		// edge : ROWID
						to_nodes_list.add( Integer.parseInt( line[0] ) );
						to_val_list.add( new Double(1.0) );
					}
				} else {					// edge : ROWID		VALUE
					to_nodes_list.add( Integer.parseInt( line[0] ) );
					to_val_list.add( Double.parseDouble( line[1] ) );
				}
*/
			}

			//for( i = 0; i < to_nodes_list.size(); i++) {
			//	output.collect( new IntWritable( to_nodes_list.get(i) ), new DoubleWritable( vector_val * to_val_list.get(i) ) );
			//}
			Iterator<Map.Entry<Integer, Double>> iter = to_map.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<Integer, Double> entry = iter.next();
				output.collect( new IntWritable( entry.getKey() ), new DoubleWritable( vector_val * entry.getValue() ) );
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // PASS 2: merge partial multiplication results
    //////////////////////////////////////////////////////////////////////
	public static class MapPass2 extends MapReduceBase
	implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		private final IntWritable from_node_int = new IntWritable();

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			from_node_int.set( Integer.parseInt(line[0]) );
			output.collect( from_node_int, new DoubleWritable( Double.parseDouble(line[1]) ) );
		}
	}

    public static class RedPass2 extends MapReduceBase
	implements Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable>
    {
		public void reduce (final IntWritable key, final Iterator<DoubleWritable> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			int i;
			double next_rank = 0;

			while (values.hasNext()) {
				String cur_value_str = values.next().toString();
				next_rank += Double.parseDouble( cur_value_str ) ;
			}

			output.collect( key, new DoubleWritable( next_rank ) );
		}
    }


    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
	protected Path vector_path = null;
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
		final int result = ToolRunner.run(new Configuration(), new VecarrvecNaiveMV(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("VecarrvecNaiveMV <edge_path> <tempmv_path> <output_path> <# of nodes> <# of reducers> <makesym or nosym>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			return printUsage();
		}


		lz_q_no = Integer.parseInt(args[0]);//edge_path = new Path(args[0]);
		vector_path = new Path(args[1]);
		tempmv_path = new Path("vecarrvec_naive_tempmv");
		output_path = new Path("vecarrvec_naive_out");				
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
		final JobConf conf = new JobConf(getConf(), VecarrvecNaiveMV.class);
		conf.set("number_nodes", "" + number_nodes);
		conf.set("makesym", "" + makesym);

		conf.setJobName("VecarrvecNaiveMV_pass1_" + job_name_base);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);

		FileInputFormat.setInputPaths(conf, vector_path);  
		for(int i = 1; i <= lz_q_no; i++) 
			FileInputFormat.addInputPaths(conf, ("lz_q" + i));  

		FileOutputFormat.setOutputPath(conf, tempmv_path);  

		conf.setNumReduceTasks( ntask );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);
		conf.setMapOutputValueClass(Text.class);

		return conf;
    }

	// Configure pass2
    protected JobConf configPass2 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), VecarrvecNaiveMV.class);
		conf.set("number_nodes", "" + number_nodes);

		conf.setJobName("VecarrvecNaiveMV_pass2_" + job_name_base);
		
		conf.setMapperClass(MapPass2.class);        
		conf.setReducerClass(RedPass2.class);
		conf.setCombinerClass(RedPass2.class);

		FileInputFormat.setInputPaths(conf, tempmv_path);  
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( ntask );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

}

