package pegasus;

/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Authors: U Kang and Christos Faloutsos

This software is licensed under Apache License, Version 2.0 (the  "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-------------------------------------------------------------------------
File: NetRaySpy.java
 - Create the spyplot data for very large graphs
Version: 2.0
***********************************************************************/


import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class NetRaySpy extends Configured implements Tool 
{
	// Digitize data
	public static class MapStage1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable>
    {
		double s_over_n = 0;
		double sm1_over_logn = 0;
		int is_linear = 0;			// linear or log scale

		public void configure(JobConf job) {
			s_over_n = Double.parseDouble(job.get("s_over_n"));
			sm1_over_logn = Double.parseDouble(job.get("sm1_over_logn"));
			is_linear = Integer.parseInt(job.get("is_linear"));

			System.out.println("MapStage1: s_over_n = " + s_over_n + ", s_over_n = " + s_over_n + ", is_linear = " + is_linear);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<Text, LongWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			if(line.length < 2 )
				return;

			int row_id = Integer.parseInt(line[0]);
			int col_id = Integer.parseInt(line[1]);
			int block_rowid = 0, block_colid = 0;

			if( is_linear == 1 ) {	// linear scale
				block_rowid = (int)Math.ceil((double)(row_id+1) * s_over_n);
				block_colid = (int)Math.ceil((double)(col_id+1) * s_over_n);
			} else {			// log scale
				block_rowid = (int)Math.ceil(Math.log10(row_id + 1) * sm1_over_logn + 0.5) ;
				block_colid = (int)Math.ceil(Math.log10(col_id + 1) * sm1_over_logn + 0.5) ;

				//System.out.println("rowid+1=" + (row_id+1) + ", sm1_over_logn = " + sm1_over_logn);
			}

			long weight = 1;
			if( line.length >= 3)
				weight = Long.parseLong( line[2] );

			output.collect( new Text("" + block_rowid + "\t" + block_colid), new LongWritable( weight ) );
		}

	}

    public static class RedStage1 extends MapReduceBase	implements Reducer<Text, LongWritable, Text, LongWritable>
    {
		public void configure(JobConf job) {
			System.out.println("RedStage1");
		}

		public void reduce (final Text key, final Iterator<LongWritable> values, final OutputCollector<Text, LongWritable> output, final Reporter reporter) throws IOException
        {
			String out_value = "";
			long sum = 0;

			while (values.hasNext()) {
				long cur_val = values.next().get();
				sum += cur_val;
			}

			output.collect(key, new LongWritable(sum) );
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
	protected Path output_path = null;
	protected int orig_nnodes = 0;
	protected int target_nnodes = 0;
	protected double s_over_n = 0;
	protected double sm1_over_logn = 0;
	
	protected int nreducer = 1;
	protected String output_prefix;
	protected int makesym = 0;
	protected int is_linear = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new NetRaySpy(), args);

//		System.exit(result);
		return;
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("NetRaySpy <edge_path> <outputedge_path> <original number of nodes> <target number of nodes> <lin or log> <# of reducer> ");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 6 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);				// input edge HDFS path
		output_path = new Path(args[1]);			// output HDFS path 
		orig_nnodes = Integer.parseInt(args[2]);	// original matrix width
		target_nnodes = Integer.parseInt(args[3]);	// target matrix width
		if( args[4].startsWith("log") )
			is_linear = 0;
		nreducer = Integer.parseInt(args[5]);

		s_over_n = (double) (target_nnodes) / (double) (orig_nnodes);
		sm1_over_logn = (double) (target_nnodes - 1) / (double) Math.log10(orig_nnodes);

		System.out.println("\n-----===[PEGASUS: A Peta-Scale Graph Mining System]===-----\n");
		System.out.println("[PEGASUS] Drawing a spy plot. original nodes = " + orig_nnodes + ", target nnodes = " + target_nnodes + ", s_over_n = " + s_over_n + ", is_linear = " + is_linear + "\n");

		// run job
		JobClient.runJob(configStage1());

		System.out.println("\n[PEGASUS] Spyplot data generated.");
		System.out.println("[PEGASUS] Output is saved in the HDFS " + args[1] + "\n");


		return 0;
    }

	// Configure pass1
    protected JobConf configStage1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), NetRaySpy.class);
		conf.set("s_over_n", "" + s_over_n);		
		conf.set("sm1_over_logn", "" + sm1_over_logn);		
		conf.set("is_linear", "" + is_linear);		
		conf.setJobName("NetRaySpy_Stage1");
		
		conf.setMapperClass(MapStage1.class);        
		conf.setReducerClass(RedStage1.class);
		conf.setCombinerClass(RedStage1.class);

		FileSystem fs = FileSystem.get(getConf());
		fs.delete(output_path, true);

		FileInputFormat.setInputPaths(conf, edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		int num_reduce_tasks = nreducer;

		conf.setNumReduceTasks( num_reduce_tasks );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(LongWritable.class);

		return conf;
    }
}

