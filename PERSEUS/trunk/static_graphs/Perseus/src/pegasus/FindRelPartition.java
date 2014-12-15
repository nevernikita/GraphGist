/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Authors: U Kang, Duen Horng Chau, and Christos Faloutsos

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
File: FindRelPartition.java
 - Plain matrix vector multiplication.
Version: 2.0
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


public class FindRelPartition extends Configured implements Tool 
{
	// Partitioner class that uses only the first token for the partition.
	// MyValueGroupingComparator and MyPartition are used for secondary-sort for building N.
	public static class MyPartition<V2> implements Partitioner<LongWritable, V2> {
		public void configure(JobConf job) {}

		public int getPartition(LongWritable key, V2 value, int numReduceTasks) {
			int partition = (int)(key.get()) % numReduceTasks;

			return partition;
		}
	}

    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text>
    {
		int partition_k = 0;
		int number_nodes = 0;
		int query_type = 0;		// 0: in neighbor, 1: out neighbor, 2: egonet

		public void configure(JobConf job) {
			partition_k = Integer.parseInt(job.get("partition_k"));
			number_nodes = Integer.parseInt(job.get("number_nodes"));
			//ignore_weights = Integer.parseInt(job.get("ignore_weights"));
			String query_type_str = job.get("query_type");
			if( query_type_str.startsWith("in") )
				query_type = 0;
			else if (query_type_str.startsWith("ou"))
				query_type = 1;
			else
				query_type = 2;


			String input_file = job.get("map.input.file");

			System.out.println("Query type=" + query_type+ "input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] tokens = line_text.split("\t");
			long nodeid = Long.parseLong(tokens[0]);
//			long dst = Long.parseLong(tokens[1]);

			long block_width = (long) (Math.ceil(((double)number_nodes)/partition_k));
			long max_block_index = (long)( (number_nodes-1)/block_width );

			if( query_type == 1 || query_type == 2 ) {	// out_neighbor or egonet
				long src_blk_index = (long)(nodeid/block_width);
				for(long col = 0; col <= max_block_index; col++) {
					long cur_partition = partition_k * src_blk_index + col;
					output.collect(new LongWritable(0), new Text("" + cur_partition) );
				}
			} 

			if(query_type == 0 || query_type == 2) {	// in neighbor or egonet. find all partitions containing column d.
				long dst_blk_index = (long)(nodeid/block_width);
				for(long row = 0; row <= max_block_index; row++) {
					long cur_partition = partition_k * row + dst_blk_index;
					output.collect(new LongWritable(0), new Text("" + cur_partition) );
				}
			}
		}
	}

	// query type: 0 (in), 1 (out), 2 (egonet)
	public static int[] getPartition( long nodeid, int number_nodes, int partition_k, String query_type_str ) {
		String result_str = "";
		int query_type;
		int i;
		int max_partitions = partition_k * partition_k;

		int []pmap = new int[max_partitions];
		for(i = 0; i < max_partitions; i++)
			pmap[i] = 0;

		if( query_type_str.startsWith("in") )
			query_type = 0;
		else if (query_type_str.startsWith("out"))
			query_type = 1;
		else
			query_type = 2;

		long block_width = (long) (Math.ceil(((double)number_nodes)/partition_k));
		long max_block_index = (long)( (number_nodes-1)/block_width );

		if( query_type == 1 || query_type == 2 ) {	// out_neighbor or egonet
			long src_blk_index = (long)(nodeid/block_width);
			for(long col = 0; col <= max_block_index; col++) {
				int cur_partition = (int)(partition_k * src_blk_index + col);
				pmap[cur_partition] = 1;
			}
		} 

		if(query_type == 0 || query_type == 2) {	// in neighbor or egonet. find all partitions containing column d.
			long dst_blk_index = (long)(nodeid/block_width);
			for(long row = 0; row <= max_block_index; row++) {
				int cur_partition = (int)(partition_k * row + dst_blk_index);
				pmap[cur_partition] = 1;
			}
		}

		int[] result = null;
		int nonzero = 0;
		for(i = 0; i < pmap.length; i++)
			if( pmap[i] != 0 )
				nonzero++;

		if( nonzero > 0 ) {
			result = new int[nonzero];
			int seq = 0;
			for(i = 0; i < pmap.length; i++) {
				if( pmap[i] != 0 ) {
					result[seq++] = i;
					System.out.println("FindRelPartitoin:: Partition " + i );
				}
			}
		}

		System.out.println("FindRelPartition:::getPartition nonzero=[" + nonzero + "]");

		return result;
	}


    public static class RedPass1 extends MapReduceBase implements Reducer<LongWritable, Text, LongWritable, Text>
    {
		int partition_k = 0;

		public void configure(JobConf job) {
			partition_k = Integer.parseInt(job.get("partition_k"));
		}

		public void reduce (final LongWritable key, final Iterator<Text> values, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
        {
			int max_partitions = partition_k * partition_k;
			int i;
			int []pmap = new int[max_partitions];
			for(i = 0; i < max_partitions; i++)
				pmap[i] = 0;

			while (values.hasNext()) {
				String line_text = values.next().toString();
				String[] tokens = line_text.split(" ");
				for(i = 0; i < tokens.length ;i++) {
					int cur_partition = Integer.parseInt(tokens[i]);
					pmap[cur_partition] = 1;
				}
			}

			String out_str = "";
			for(i = 0; i < max_partitions; i++) {
				if( pmap[i] != 0 ) {
					if(out_str.length() != 0 )
						out_str += " ";
					out_str += "" + i;
				}
			}
			
			if(out_str.length() > 0 )
				output.collect(new LongWritable(0), new Text(out_str) );
		}
    }


    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
    protected Path tempmv_path = null;
	protected Path output_path = null;
	protected Path vector_path = null;
	protected int number_nodes = 0;
	protected int nreducer = 1;
	int makesym = 0;
	int transpose = 0;
	int ignore_weights = 0;
	int partition_k = 0;
	String query_type_str = "";

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new FindRelPartition(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("FindRelPartition <vector_path> <output_path> <# of nodes> <partition_k> <query_type: innh or outnh or egonet>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 5 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);
		output_path = new Path(args[1]);				
		number_nodes = Integer.parseInt(args[2]);
		partition_k = Integer.parseInt(args[3]);
		nreducer = partition_k * partition_k;
		query_type_str = args[4];

		System.out.println("Starting FindRelPartition. edge_path=" + args[0] );

		JobClient.runJob(configPass1());

		System.out.println("Done. output is saved in HDFS " + args[1]);

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), FindRelPartition.class);
		conf.set("partition_k", "" + partition_k);
		conf.set("number_nodes", "" + number_nodes);
		conf.set("query_type", "" + query_type_str);

		conf.setJobName("FindRelPartition");
		System.out.println("Configuring FindRelPartition. partition_k=" + partition_k);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setCombinerClass(RedPass1.class);
		conf.setReducerClass(RedPass1.class);
		//conf.setPartitionerClass(MyPartition.class);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(output_path);

		FileInputFormat.setInputPaths(conf, edge_path);  
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( 1 );

//		conf.setOutputKeyClass(Text.class);
//		conf.setOutputValueClass(Text.class);
		conf.setMapOutputKeyClass(LongWritable.class);
		conf.setMapOutputValueClass(Text.class);

		return conf;
    }
}

