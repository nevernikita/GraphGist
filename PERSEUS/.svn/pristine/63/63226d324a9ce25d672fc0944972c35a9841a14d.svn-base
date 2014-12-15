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
File: MatvecNaiveSecondarySort.java
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


public class MatvecNaiveSecondarySort extends Configured implements Tool 
{

	// Comparator class that uses only the first token for the comparison.
	// MyValueGroupingComparator and MyPartition are used for secondary-sort for building N.
	//     reference: src/examples/org/apache/hadoop/examples/SecondarySort.java 
	public static class MyValueGroupingComparator implements RawComparator<Text> {
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		  return WritableComparator.compareBytes(b1, s1, Integer.SIZE/8, 
												 b2, s2, Integer.SIZE/8);
		}
		public int compare(Text t1, Text t2) {
			String str1 = t1.toString();
			String str2 = t2.toString();
			int pos1 = str1.indexOf("\t");
			int pos2 = str2.indexOf("\t");
			long l = (Long.parseLong(str1.substring(0, pos1)));
			long r = (Long.parseLong(str2.substring(0, pos2)));

			return l == r ? 0 : (l < r ? -1 : 1);
		}
	}

	public static class MyKeyComparator implements RawComparator<Text> {
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		  return WritableComparator.compareBytes(b1, s1, Integer.SIZE/8, 
												 b2, s2, Integer.SIZE/8);
		}
		public int compare(Text t1, Text t2) {
			String str1 = t1.toString();
			String str2 = t2.toString();
			int pos1 = str1.indexOf("\t");
			int pos2 = str2.indexOf("\t");
			long l = (Long.parseLong(str1.substring(0, pos1)));
			long r = (Long.parseLong(str2.substring(0, pos2)));

			if( l != r )
				return (l < r ? -1 : 1);

			l = Long.parseLong(str1.substring(pos1 + 1));
			r = Long.parseLong(str2.substring(pos2 + 1));
			
			return l == r ? 0 : (l < r ? -1 : 1);
		}
	}


	// Partitioner class that uses only the first token for the partition.
	// MyValueGroupingComparator and MyPartition are used for secondary-sort for building N.
	public static class MyPartition<V2> implements Partitioner<Text, V2> {
		public void configure(JobConf job) {}

		public int getPartition(Text key, V2 value, int numReduceTasks) {
			//System.out.println("[DEBUG] getPartition. key=" + key );
			String []tokens = key.toString().split("\t");
			int partition = (int)(Long.parseLong(tokens[0])) % numReduceTasks;

			return partition;
		}
	}

    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>
    {
		int makesym = 0;
		int transpose = 0;
		int ignore_weights = 0;

		public void configure(JobConf job) {
			makesym = Integer.parseInt(job.get("makesym"));
			transpose = Integer.parseInt(job.get("transpose"));
			ignore_weights = Integer.parseInt(job.get("ignore_weights"));

			String input_file = job.get("map.input.file");

			System.out.println("MatvecNaiveSecondarySort.MapPass1: makesym = " + makesym);
			System.out.println("input_file = " + input_file);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<Text, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			//System.out.println("[DEBUG] line_text=[" + line_text  + "]");

			if( line.length == 2 || ignore_weights == 1) {	// vector : ROWID	VALUE('vNNNN')
				if( line[1].charAt(0) == 'v' ) {	// vector : ROWID	VALUE('vNNNN')
					String key_str = line[0] + "\t0";
					//System.out.println("[DEBUG] MapPass1 key=[" + key_str + "], value=[" + line[1] + "]");
					output.collect( new Text(key_str), new Text(line[1]) );
				} else {					// edge : ROWID		COLID
					if(transpose == 0) {
						//System.out.println("[DEBUG] MapPass1 key=[" + line[1] + "\t1], value=[" + line[0] + "]");
						output.collect( new Text(line[1] + "\t1"), new Text(line[0]) );
						if(makesym == 1)
							output.collect( new Text(line[0] + "\t1"), new Text(line[1]) );
					} else {
						output.collect( new Text(line[0] + "\t1"), new Text(line[1]) );
						if(makesym == 1)
							output.collect( new Text(line[1] + "\t1"), new Text(line[0]) );
					}
				}
			} else if(line.length == 3) {					// edge: ROWID    COLID    VALUE
				if(transpose == 0) {
					String key_str = line[1] + "\t1";
					String value_str = line[0] + "\t" + line[2];
//					System.out.println("[DEBUG] MapPass1 key=[" + key_str + "], value=[" + value_str + "]");

					output.collect( new Text(key_str), new Text(value_str) );
					if(makesym == 1)
						output.collect( new Text(line[0] + "\t1"), new Text(line[1] + "\t" + line[2]) );
				} else {
					output.collect( new Text(line[0] + "\t1"), new Text(line[1] + "\t" + line[2]) );
					if(makesym == 1)
						output.collect( new Text(line[1] + "\t1"), new Text(line[0] + "\t" + line[2]) );
				}
			}
		}
	}

    public static class RedPass1 extends MapReduceBase implements Reducer<Text, Text, LongWritable, DoubleWritable>
    {
		public void reduce (final Text key, final Iterator<Text> values, final OutputCollector<LongWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
//			int i;
			double vector_val = 0;
			boolean isValReceived = false;
						
			while (values.hasNext()) {
				String line_text = values.next().toString();

				//System.out.println("[DEBUG] RedPass1. Key=[" + key + "], val=[" + line_text + "]");
				
				if( isValReceived == false ) {
					if( line_text.charAt(0) == 'v' ) {
						vector_val = Double.parseDouble( line_text.substring(1) );
						//System.out.println("[DEBUG] RedPass1. HAPPY EVENT Key=[" + key + "], val=[" + line_text + "]");
					} else {
						return;
						//System.out.println("[DEBUG] RedPass1. FATAL ERROR Key=[" + key + "], val=[" + line_text + "]");
						//vector_val = Double.parseDouble( line_text );
					}
					isValReceived = true;
				} else {
					String []tokens = line_text.split("\t");

					if( tokens.length == 1 ) {	// edge : ROWID
						//System.out.println("[DEBUG] RedPass1. outputting key=[" + tokens[0] + "], val=[" + vector_val + "]");

						output.collect(new LongWritable(Long.parseLong(tokens[0])), new DoubleWritable(vector_val) );
					} else {					// edge : ROWID		VALUE
						output.collect(new LongWritable(Long.parseLong(tokens[0])), new DoubleWritable(vector_val * Double.parseDouble(tokens[1])) );
					}
				}
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // PASS 2: merge partial multiplication results
    //////////////////////////////////////////////////////////////////////
	public static class MapPass2 extends MapReduceBase
	implements Mapper<LongWritable, Text, LongWritable, Text>
    {
		private final LongWritable from_node_int = new LongWritable();

		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			from_node_int.set( Long.parseLong(line[0]) );
			output.collect( from_node_int, new Text("v" + Double.parseDouble(line[1]) ) );
		}
	}

    public static class RedPass2 extends MapReduceBase
	implements Reducer<LongWritable, Text, LongWritable, Text>
    {
		public void reduce (final LongWritable key, final Iterator<Text> values, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
        {
			int i;
			double next_rank = 0;

			while (values.hasNext()) {
				String cur_value_str = values.next().toString();
				next_rank += Double.parseDouble( cur_value_str.substring(1) ) ;
			}

			output.collect( key, new Text( "v" + next_rank ) );
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

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MatvecNaiveSecondarySort(), args);

		System.exit(result);
    }

	// matrix-vecotr multiplication with secondary sort
	public static void MatvecNaiveSS(Configuration conf, int nreducer, String mat_path, String vec_path, String out_path, int transpose, int makesym) throws Exception {
		System.out.println("Running MatvecNaiveSS: mat_path=" + mat_path + ", vec_path=" + vec_path);
		int ignore_weights = 0;

		String [] args = new String[8];
		args[0] = new String( "" + mat_path);
		args[1] = new String( "temp_matvecnaive_ss" + vec_path);
		args[2] = new String(out_path);
		args[3] = new String( "" + nreducer );
		if( makesym == 1 )
			args[4] = "makesym";
		else
			args[4] = "nosym";
		args[5] = new String( vec_path);
		args[6] = new String( "" + transpose);
		args[7] = new String( "" + ignore_weights);

		ToolRunner.run(conf, new MatvecNaiveSecondarySort(), args);
		System.out.println("Done MatvecNaiveSS. Output is saved in HDFS " + out_path);

		return;
	}

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MatvecNaiveSecondarySort <edge_path> <tempmv_path> <output_path> <# of reducers> <makesym or nosym> <vector path>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length < 5 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);
		tempmv_path = new Path(args[1]);
		output_path = new Path(args[2]);				
		nreducer = Integer.parseInt(args[3]);
		if(args[4].equals("makesym"))
			makesym = 1;
		if( args.length > 5 )
			vector_path = new Path(args[5]);
		if( args.length > 6 )
			transpose = Integer.parseInt(args[6]);
		if( args.length > 7 )
			ignore_weights = Integer.parseInt(args[7]);

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(tempmv_path);
		fs.delete(output_path);

		System.out.println("Starting MatvecNaiveSecondarySort. tempmv_path=" + args[1] + ", transpose=" + transpose);

		JobClient.runJob(configPass1());
		JobClient.runJob(configPass2());

		fs.delete(tempmv_path);
		System.out.println("Done. output is saved in HDFS " + args[2]);

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecNaiveSecondarySort.class);
		conf.set("number_nodes", "" + number_nodes);
		conf.set("makesym", "" + makesym);
		conf.set("transpose", "" + transpose);
		conf.set("ignore_weights", "" + ignore_weights);

		conf.setJobName("MatvecNaiveSecondarySort_pass1");
		System.out.println("Configuring MatvecNaiveSecondarySort. makesym=" + makesym);
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);
		conf.setPartitionerClass(MyPartition.class);
		conf.setOutputValueGroupingComparator(MyValueGroupingComparator.class);
//		conf.setOutputKeyComparatorClass(MyKeyComparator.class);

		if( vector_path == null )
			FileInputFormat.setInputPaths(conf, edge_path);  
		else
			FileInputFormat.setInputPaths(conf, edge_path, vector_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, tempmv_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(LongWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);
		conf.setMapOutputKeyClass(Text.class);
		conf.setMapOutputValueClass(Text.class);

		return conf;
    }

	// Configure pass2
    protected JobConf configPass2 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecNaiveSecondarySort.class);
		conf.set("number_nodes", "" + number_nodes);

		conf.setJobName("MatvecNaive_pass2");
		
		conf.setMapperClass(MapPass2.class);        
		conf.setReducerClass(RedPass2.class);
		conf.setCombinerClass(RedPass2.class);

		FileInputFormat.setInputPaths(conf, tempmv_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(LongWritable.class);
		//conf.setMapOutputValueClass(DoubleWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }

}

