/*******************************************************************************
QToRitzMatrix.java - Naive matrix-vector multiplication in hadoop.
Copyright (c) 2009 U Kang(ukang@cs.cmu.edu)
All rights preserved

You may use this code without fee, for educational or research purposes.
You agree to cite the following paper whenever you use the code:
 *** John Tomson and Tom Johson: 
 *** "Graph mining for fun and profit", 
 *** Proc. ACM KDD, Las Vegas, Aug. 2008 

Any for-profit use requires the consent of the author(s).


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

public class QToRitzMatrix extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		int col = 0;

		public void configure(JobConf job) {
			//makesym = Integer.parseInt(job.get("makesym"));
			//String left_filename = job.get("left_filename");
			//String right_filename = job.get("right_filename");
			String input_file = job.get("map.input.file");
			String right_vec_str = job.get("right_vector");

			int lzq_index = input_file.indexOf("lz_q");
			int slash_index = input_file.indexOf("/", lzq_index);
			col = Integer.parseInt( input_file.substring(lzq_index + 4, slash_index) ) - 1;

			System.out.println("QToRitzMatrix.MapPass1: col=" + col);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			// the format is ROW VALUE

			double out_val = 0;
			if( line[1].charAt(0) == 'v' )
				out_val = Double.parseDouble(line[1].substring(1));
			else
				out_val = Double.parseDouble(line[1]);

			output.collect( new IntWritable(Integer.parseInt(line[0])), new Text("" + col + "\t" + out_val) );
			
		}
	}

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
	protected int number_nodes = 0;
	protected int nreducers = 1;
	protected String edge_file_name;
	protected String job_name_base;
	protected int lz_q_no = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new QToRitzMatrix(), args);

		return;
//		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("QToRitzMatrix <left edge_path> <# of reducers> <right edge file> <m> <n>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		lz_q_no = Integer.parseInt(args[0]);//edge_path = new Path(args[0]);
		nreducers = Integer.parseInt(args[1]);
		Path out_path = new Path(args[2]);
		String base_path_name = args[3];
		if( base_path_name.equals("null"))
			base_path_name = "";

		System.out.println("Running QToRitzMatrix...");
		JobClient.runJob( configPass1(out_path, base_path_name) );
		System.out.println("QToRitzMatrix Done. Output is saved in HDFS " + out_path.getName() );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 (Path out_path, String base_path_name) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), QToRitzMatrix.class);
		conf.setJobName("QToRitzMatrix_pass1_" + job_name_base);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(PegasusUtils.RedIdentity.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(out_path);


		FileInputFormat.setInputPaths(conf, base_path_name + "lz_q1");  
		for(int i = 2; i <= lz_q_no; i++) 
			FileInputFormat.addInputPaths(conf, (base_path_name + "lz_q" + i));  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }


}

