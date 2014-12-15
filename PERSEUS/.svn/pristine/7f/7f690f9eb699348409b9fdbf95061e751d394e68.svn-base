/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: InitB.java
 - HCC: Generate the initial vector for the PageRank.
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


public class InitB extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // STAGE 1: make initial pagerank vector
    //////////////////////////////////////////////////////////////////////
	public static class MapStage1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1] + "\t" + line[2]) );
		}
	}

    public static class RedStage1 extends MapReduceBase	implements Reducer<IntWritable, Text, IntWritable, Text>
    {
		int number_nodes = 1;
		double initial_weight = 0.0f;
		String str_weight;
		private final IntWritable from_node_int = new IntWritable();

		public void configure(JobConf job) {
			number_nodes = Integer.parseInt(job.get("number_nodes"));
			initial_weight = (double)1.0 / (double)Math.sqrt(number_nodes);
			str_weight = new String("" + initial_weight );
			System.out.println("MapStage1: number_nodes = " + number_nodes);
		}

		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
        {
			int i;

			while (values.hasNext()) {
				String cur_value_str = values.next().toString();
				final String[] line = cur_value_str.split("\t");

				int start_node = Integer.parseInt(line[0]);
				int end_node = Integer.parseInt(line[1]);

				for(i = start_node; i <= end_node; i++) {
					from_node_int.set( i );
					output.collect( from_node_int, new Text("v" + Math.random()) );//initial_weight));
				}
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected Path output_path = null;
	protected Path initial_b_path = new Path("lanczos_b_input");
	protected int number_nodes = 0;
	protected int nreducers = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new InitB(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("InitB <output_path> <# of nodes> <# of reducers>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			System.out.println("args.length = " + args.length);
			int i;
			for(i=0; i < args.length; i++) {
				System.out.println("args[" + i + "] = " + args[i] );
			}
			return printUsage();
		}

		output_path = new Path(args[0]);				
		number_nodes = Integer.parseInt(args[1]);
		nreducers = Integer.parseInt(args[2]);

		System.out.print("Generating initial bitmask for " + number_nodes + " nodes ");
		// create bitmask generate command file, and copy to curpr_path
		gen_initial_b_file(number_nodes, nreducers, initial_b_path);
		System.out.println(" done");

		JobClient.runJob(configStage1());

		return 0;
    }

	// create PageRank init vector generation command
	public void gen_initial_b_file(int number_nodes, int nmachines, Path initial_input_path) throws IOException
	{
		int gap = number_nodes / nmachines;
		int i;
		int start_node, end_node;
		String file_name = "lanczos.initial_b.temp";
		FileWriter file = new FileWriter(file_name);
		BufferedWriter out = new BufferedWriter (file);

		out.write("# number of nodes in graph = " + number_nodes+"\n");
		System.out.println("creating initial b vector (total nodes = " + number_nodes + ")");

		for(i=0; i < nmachines; i++)
		{
			start_node = i * gap;
			if( i < nmachines - 1 )
				end_node = (i+1)*gap - 1;
			else
				end_node = number_nodes - 1;

			out.write("" + i + "\t" + start_node + "\t" + end_node + "\n" );
        }
		out.close();

		// copy it to initial_input_path, and delete the temporary local file.
		final FileSystem fs = FileSystem.get(getConf());
		fs.copyFromLocalFile( true, new Path("./" + file_name), new Path (initial_input_path.toString()+ "/" + file_name) );
	}

    // Configure pass1
    protected JobConf configStage1() throws Exception
    {
		final JobConf conf = new JobConf(getConf(), HEigen.class);
		conf.set("number_nodes", "" + number_nodes);
		conf.setJobName("InitB_Stage1");

		conf.setMapperClass(MapStage1.class);
		conf.setReducerClass(RedStage1.class);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(output_path, true);


		FileInputFormat.setInputPaths(conf, initial_b_path);  
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumMapTasks( nreducers );
		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setMapOutputValueClass(Text.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }
}

