/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: MakeSymEdge.java
 - Given a directed graph, make symmetric edge with duplicates eliminated
Version: 2.0
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

public class MakeSymEdge extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // STAGE 1: remove self loop, make symmetric, and remove duplicated edges
	//  - Input: edge_file (src, dst, [optionally, weight] )
	//           Assume the edge is in sparse format. That is, only the edge (A, B) exists where (id(A) < id(B)).
	//  - Output: unique_edges (src, dst, [weight]), (dst, src, [weight])
    //////////////////////////////////////////////////////////////////////
	public static class MapStage1 extends MapReduceBase	implements Mapper<LongWritable, Text, Text, Text>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<Text, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			if(line.length < 2 )
				return;

			long src_id = Long.parseLong(line[0]);
			long dst_id = Long.parseLong(line[1]);

			if( src_id != dst_id ) {
				if( line.length == 2 ) {
					output.collect( new Text(src_id + "\t" + dst_id), new Text("") );
					output.collect( new Text(dst_id + "\t" + src_id), new Text("") );
				} else {
					output.collect( new Text(src_id + "\t" + dst_id), new Text(line[2]) );
					output.collect( new Text(dst_id + "\t" + src_id), new Text(line[2]) );
				}
			}
		}
	}

    public static class RedStage1 extends MapReduceBase	implements Reducer<Text, Text, Text, Text>
    {
		public void reduce (final Text key, final Iterator<Text> values, final OutputCollector<Text, Text> output, final Reporter reporter) throws IOException
        {
			String cur_value_str = "";
			while (values.hasNext()) {
				cur_value_str = values.next().toString();
			}

			output.collect(key, new Text(cur_value_str));
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
	protected Path unique_edge_path = new Path("unique_edge");
	protected Path partial_deg_path = null;
	protected Path output_path = null;
	protected Path node_deg_path = new Path("node_deg");
	protected int number_nodes = 0;
	protected int niteration = 32;
	protected double mixing_c = 0.85f;
	protected int nreducers = 1;
	protected String edge_file_name;
	protected String job_name_base;
	protected int make_symmetric = 0;		// convert directed graph to undirected graph
	protected String deg_type = "";
	protected int remove_duplicate = 1;		// if 1, it removes duplicate when calculating inout-degree.

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MakeSymEdge(), args);

		return;
//		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MakeSymEdge <input edge_path> <output_path> <# of reducers>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			return printUsage();
		}

		int i;
		edge_path = new Path(args[0]);
		output_path = new Path(args[1]);				
		nreducers = Integer.parseInt(args[2]);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(output_path, true);

		// Run Stage1 and Stage2.
		System.out.println("Converting to symmetric edge...");
		JobClient.runJob(configStage1());

		System.out.println("The symmetric graphs is saved in the HDFS " + args[1] + ".");

		return 0;
    }


	// Configure pass1
    protected JobConf configStage1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MakeSymEdge.class);
		conf.setJobName("MakeSymEdge");
		
		conf.setMapperClass(MapStage1.class);        
		conf.setReducerClass(RedStage1.class);

		FileInputFormat.setInputPaths(conf, edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }

}

