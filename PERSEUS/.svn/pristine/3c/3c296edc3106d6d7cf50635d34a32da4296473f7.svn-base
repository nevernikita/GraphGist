/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: ScalarMultBlock.java
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


public class ScalarMultBlock extends Configured implements Tool 
{
	public static class MapStage1Text extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		private final IntWritable from_node_int = new IntWritable();
		private boolean isYpath = false;
		private boolean isXpath = false;
		private double s;
		protected int block_width = 16;

		public void configure(JobConf job) {
			s = Double.parseDouble(job.get("s"));
			block_width = Integer.parseInt(job.get("block_width"));

			System.out.println("ScalarMult.MapStage1: s = " + s);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");

			int out_key = Integer.parseInt(line_text.substring(0, tabpos));
			String val_str = line_text.substring(tabpos+1);

			double []xvec = MatvecUtils.decodeBlockVector(val_str, block_width) ;
			for(int i = 0; i < block_width; i++) {
				if( xvec[i] != 0 )
					xvec[i] = xvec[i] * s;
			}

			String new_val_str = MatvecUtils.encodeBlockVector(xvec, block_width);
			output.collect( new IntWritable(out_key) , new Text( new_val_str ) );
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
		final int result = ToolRunner.run(new Configuration(), new ScalarMultBlock(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("ScalarMultBlock <# of reducers> <y_path> <x_path> <a>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			return printUsage();
		}

		Path in_path = new Path(args[0]);
		double s = Double.parseDouble(args[1]);
		block_width = Integer.parseInt(args[2]);

		final FileSystem fs = FileSystem.get(getConf());

		Path smult_output = new Path("smult_output");
		fs.delete(smult_output);

		System.out.println("\nRunning scalar_mult... in_path=" + in_path.getName() + ", s=" + s);
		JobClient.runJob( configScalarMult(in_path, smult_output, s) );
		System.out.println("ScalarMult done. Output is saved in HDFS " + smult_output.getName() );

		return 0;

    }

	// Configure ScalarMult
    protected JobConf configScalarMult (Path in_path, Path smult_output, double s) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), HEigen.class);
		conf.set("s", "" + s);
		conf.set("block_width", "" + block_width);
		conf.setJobName("Lanczos_pass_smult");

		conf.setMapperClass(MapStage1Text.class);        

		FileInputFormat.setInputPaths(conf, in_path);  
		FileOutputFormat.setOutputPath(conf, smult_output);  

		conf.setNumReduceTasks( 0 );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);//conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

}

