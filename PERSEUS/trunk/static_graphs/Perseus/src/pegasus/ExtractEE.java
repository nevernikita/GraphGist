/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: ExtractEE.java
 - Lanczos
Version: 0.9
Author Email: U Kang(ukang@cs.cmu.edu), Christos Faloutsos(christos@cs.cmu.edu)
***********************************************************************/
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


public class ExtractEE extends Configured implements Tool 
{
	// MapStage1: 
	public static class MapStage1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		private boolean isP1 = false;
		private boolean isP2 = false;

		public void configure(JobConf job) {
			String p1_name = job.get("p1_path");
			String p2_name = job.get("p2_path");

			String input_file = job.get("map.input.file");
			if(input_file.contains(p1_name))
				isP1 = true;
			else if(input_file.contains(p2_name))
				isP2 = true;

			System.out.println("ExtractEE.MapStage1: map.input.file = " + input_file + ", isP1=" + isP1 + ", isP2=" + isP2);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			String []tokens = line_text.split("\t");

			String out_val = "";
			if( isP1 )
				out_val += "X";
			else
				out_val += "Y";

			out_val += tokens[1];

			output.collect( new IntWritable(Integer.parseInt(tokens[0])) , new Text(out_val) );
		}
	}

		

	// RedStage1
    public static class RedStage1 extends MapReduceBase	implements Reducer<IntWritable, Text, DoubleWritable, DoubleWritable>
    {
		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<DoubleWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double x_val = 0;
			double y_val = 0;

			while (values.hasNext()) {
				String cur_value_str = values.next().toString();
				
				double cur_double = 0;
				if( cur_value_str.charAt(1) == 'v' )
					cur_double = Double.parseDouble( cur_value_str.substring(2) );
				else
					cur_double = Double.parseDouble( cur_value_str.substring(1) );

				if( cur_value_str.charAt(0) == 'X')
					x_val = cur_double;
				else
					y_val = cur_double;
			}

			output.collect(new DoubleWritable(x_val), new DoubleWritable(y_val));
		}
    }



    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected int nreducers = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new ExtractEE(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("ExtractEE <in_path>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		String p1_name = args[0];
		String p2_name = args[1];
		String out_name = args[2];
		int nreducer = Integer.parseInt(args[3]);

		System.out.println("\nRunning ExtractEE... p1=" + p1_name + ", p2=" + p2_name + ", out=" + out_name);

		Path p1_path = new Path(p1_name);
		Path p2_path = new Path(p2_name);
		Path out_path = new Path(out_name);

		// Extract EE
		final FileSystem fs = FileSystem.get(getConf());
		//Path extractee_output = new Path("extractee_output");
		fs.delete(out_path);
		JobClient.runJob(configExtractEE(p1_path, p2_path, out_path, nreducer));

		System.out.println("ExtractEE done. Output is saved in HDFS " + out_path.getName() );


		return 0;
    }

    protected JobConf configExtractEE (Path p1_path, Path p2_path, Path out_path, int nreducer) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), ExtractEE.class);
		conf.setJobName("ExtractEE");
		conf.set("p1_path", p1_path.getName() );
		conf.set("p2_path", p2_path.getName() );

		conf.setMapperClass(MapStage1.class);        
		conf.setReducerClass(RedStage1.class);

		FileInputFormat.setInputPaths(conf, p1_path, p2_path);  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setMapOutputKeyClass(IntWritable.class);
		conf.setMapOutputValueClass(Text.class);
		conf.setOutputKeyClass(DoubleWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}

