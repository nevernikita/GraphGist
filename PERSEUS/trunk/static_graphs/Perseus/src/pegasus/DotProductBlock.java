/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: DotProductBlock.java
 - Lanczos
Version: 0.9
Author Email: U Kang(ukang@cs.cmu.edu), Christos Faloutsos(christos@cs.cmu.edu)
***********************************************************************/
package pegasus;

import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class DotProductBlock extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // STAGE 1: make initial pagerank vector
    //////////////////////////////////////////////////////////////////////

	// MapStage1: PegasusUtils.MapIdentity.class

	// RedStage1
    public static class RedStage1 extends MapReduceBase	implements Reducer<IntWritable, Text, IntWritable, Text>
    {
		protected int block_width = 16;

		public void configure(JobConf job) {
			block_width = Integer.parseInt(job.get("block_width"));
			System.out.println("DotProductBlock:RedStage1 : configure is called. block_width=" + block_width);
		}

		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
        {
			double[] v1 = null, v2 = null;
			int input_index= 0;

			while (values.hasNext()) {
				String cur_value_str = values.next().toString();

				//System.out.println("[DEBUG] DotProductBlock cur_value_str=" + cur_value_str);

				if(input_index == 0 ) {
					v1 = MatvecUtils.decodeBlockVector( cur_value_str, block_width );
					input_index++;
				} else
					v2 = MatvecUtils.decodeBlockVector( cur_value_str, block_width );
			}

			int i;
			double result = 0;

			if( input_index == 2 ) {
				for(i = 0; i < block_width; i++ )
					result += v1[i] * v2[i] ;

				if( result != 0 )
					output.collect(new IntWritable(0), new Text("" + result));
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected int nreducers = 1;
	int block_width = 16;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new DotProductBlock(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("DotProductBlock <# of reducers> <y_path> <x_path> <a>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		int ret_val = 0;

		nreducers = Integer.parseInt(args[0]);
		Path p1 = new Path(args[1]);
		Path p2 = new Path(args[2]);
		block_width = Integer.parseInt(args[3]);


		final FileSystem fs = FileSystem.get(getConf());
		System.out.println("\nRunning DotProductBlock... p1=" + p1.getName() + ", p2=" + p2.getName());

		Path dp_output1 = new Path("dp_output1");
		JobClient.runJob(configDotproduct1(nreducers, p1, p2, dp_output1));

		Path dp_output2 = new Path("dp_output2");
		JobClient.runJob(configDotproduct2(nreducers, dp_output1, dp_output2));

		return 0;
    }

	// Configure dot product 1/2
    protected JobConf configDotproduct1 (int nreducer, Path p1, Path p2, Path dp_output1) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DotProductBlock.class);
		conf.set("block_width", "" + block_width);
		conf.setJobName("Lanczos_pass_dotproduct");
		
		conf.setMapperClass(PegasusUtils.MapIdentity.class);        
		conf.setReducerClass(DotProductBlock.RedStage1.class);

		FileSystem fs = FileSystem.get(getConf());
		fs.delete(dp_output1);

		FileInputFormat.setInputPaths(conf, p1, p2);  
		FileOutputFormat.setOutputPath(conf, dp_output1);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }

	// Configure dot product 2/2
    protected JobConf configDotproduct2 (int nreducer, Path dp_output1, Path dp_output2) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DotProductBlock.class);
		//conf.set("number_nodes", "" + number_nodes);
		conf.setJobName("Lanczos_pass_dotproduct");
		
		conf.setMapperClass(PegasusUtils.MapIdentityDouble.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		FileSystem fs = FileSystem.get(getConf());
		fs.delete(dp_output2);

		FileInputFormat.setInputPaths(conf, dp_output1);  
		FileOutputFormat.setOutputPath(conf, dp_output2);  

		conf.setNumReduceTasks( 1 );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }


/*
    public int run (final String[] args) throws Exception
    {
		return 0;
    }

    // Configure pass1
    protected JobConf configStage1() throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DotProductBlock.class);
		conf.set("number_nodes", "" + number_nodes);
		conf.setJobName("DotProductBlock_Stage1");

		conf.setMapperClass(MapStage1.class);
		conf.setReducerClass(RedStage1.class);

		FileInputFormat.setInputPaths(conf, initial_b_path);  
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumMapTasks( nreducers );
		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setMapOutputValueClass(Text.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }
*/
}

