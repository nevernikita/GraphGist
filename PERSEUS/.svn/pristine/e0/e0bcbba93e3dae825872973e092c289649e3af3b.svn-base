/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: L2normBlock.java
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


public class L2normBlock extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // STAGE 1: make initial pagerank vector
    //////////////////////////////////////////////////////////////////////

	// MapStage1: PegasusUtils.MapIdentity.class

	// RedStage1
	public static class MapStage1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		protected int block_width = 16;

		public void configure(JobConf job) {
			block_width = Integer.parseInt(job.get("block_width"));
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");
			String val_str = line_text.substring(tabpos+1);

			double []xvec = MatvecUtils.decodeBlockVector(val_str, block_width) ;

			double sum = 0;
			for(int i = 0; i < block_width; i++) {
				if( xvec[i] != 0 )
					sum += xvec[i] * xvec[i];
			}

			output.collect( new IntWritable(0) , new DoubleWritable( sum ) );
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
		final int result = ToolRunner.run(new Configuration(), new L2normBlock(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("L2normBlock <# of reducers> <y_path> <x_path> <a>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 2 ) {
			return printUsage();
		}

		Path in_path = new Path(args[0]);
		block_width = Integer.parseInt(args[1]);

		System.out.println("\nRunning L2normBlock... in_path=" + in_path.getName() );
		final FileSystem fs = FileSystem.get(getConf());
		Path l2norm_output = new Path("l2norm_output");
		fs.delete(l2norm_output);
		JobClient.runJob(configL2norm(in_path, l2norm_output));

		System.out.println("L2norm done. L2norm^2 is saved in HDFS " + l2norm_output.getName() );

		return 0;
    }

	// Configure l2 norm
    protected JobConf configL2norm (Path in_path, Path out_path) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), HEigen.class);
		conf.set("block_width", "" + block_width);
		conf.setJobName("L2norm");
		
		conf.setMapperClass(MapStage1.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		FileInputFormat.setInputPaths(conf, in_path);  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( 1 );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}

