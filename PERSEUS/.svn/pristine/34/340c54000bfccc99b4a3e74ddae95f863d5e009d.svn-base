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
File: PegasusUtils.java
 - Common utility classes and functions
Version: 2.0
***********************************************************************/

package pegasus;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import sun.misc.*;
import org.apache.hadoop.filecache.*;
import java.net.*;


//class MinMaxInfo
//{
//	public double min;
//	public double max;
//};


// common utility functions
public class PegasusUtils
{
	public static BufferedWriter open_log_file(String job_name_base) throws Exception
	{
		FileWriter fstream = new FileWriter(job_name_base + ".log");
		BufferedWriter out = new BufferedWriter(fstream);

		return out;
	}

	public static String get_cur_datetime()
	{
		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	public static String format_duration(long millis)
	{
		String DATE_FORMAT_NOW = "HH:mm:ss";

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(millis));
	}

	public static int min2( int a, int b ) {
		if( a < b )
			return a;

		return b;
	}
	
	public static int max2( int a, int b ) {
		if( a > b )
			return a;

		return b;
	}


	/////////////////////////////////////////////////////////////////////////
	// Matrix-Vector Multiplications
	//
	public static void MatvecNaive(Configuration conf, int nreducer, String mat_path, String vec_path, String out_path, int transpose, int ignore_weights) throws Exception {
		System.out.println("Running Matvecnaive: mat_path=" + mat_path + ", vec_path=" + vec_path);
		String [] args = new String[8];
		args[0] = new String( "" + mat_path);
		args[1] = new String( "temp_mv");
		args[2] = new String(out_path);
		args[3] = new String( "" + nreducer );
		args[4] = "nosym";
		args[5] = new String( vec_path);
		args[6] = new String( "" + transpose);
		args[7] = new String( "" + ignore_weights);

		ToolRunner.run(conf, new MatvecNaive(), args);
		System.out.println("Done Matvecnaive. Output is saved in HDFS " + out_path);

		return;
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

	// matrix-vecotr multiplication with secondary sort
	public static void MatvecNaiveDcache(Configuration conf, String edge_path, String local_vector_file, String output_path, int nreducer, int transpose, int makesym) throws Exception {
		System.out.println("Running MatvecNaiveDcache: edge_path=" + edge_path + ", local_vector_file=" + local_vector_file + ", output_path=" + output_path + ", nreducer=" + nreducer + ", transpose=" + transpose);

		String [] args = new String[6];
		args[0] = edge_path;
		args[1] = local_vector_file;
		args[2] = output_path;
		args[3] = "" + nreducer;
		args[4] = "" + transpose;
		if( makesym == 1 )
			args[5] = "makesym";
		else
			args[5] = "nosym";

		ToolRunner.run(conf, new MatvecNaiveDcache(), args);
		//System.out.println("Done MatvecNaiveDcache. Output is saved in HDFS " + output_path);

		return;
	}

	// matrix-vecotr multiplication, block
	public static void MatvecBlock(Configuration conf, String mat_path, String vec_path, String out_path,  String edge_type, int block_width, int nreducer, int transpose, int makesym) throws Exception {
		System.out.println("Running MatvecBlock: mat_path=" + mat_path + ", vec_path=" + vec_path);

		String [] args = new String[9];
		args[0] = mat_path;
		args[1] = vec_path;
		args[2] = "temp_matvecblock_" + vec_path;
		args[3] = out_path;
		args[4] = "" + nreducer;
		args[5] = edge_type;
		args[6] = "" + block_width;
		args[7] = "" + transpose;
		if( makesym == 1 )
			args[8] = "makesym";
		else
			args[8] = "nosym";

		ToolRunner.run(conf, new MatvecBlockSS(), args);
		//System.out.println("Done MatvecNaiveSS. Output is saved in HDFS " + out_path);

		return;
	}


	// matrix-vecotr multiplication, block
	public static void MatvecBlockDcache(Configuration conf, String mat_path, String vec_path, String out_path,  String edge_type, int block_width, int nreducer, int transpose, int makesym) throws Exception {
		System.out.println("Running MatvecBlockDcache: mat_path=" + mat_path + ", vec_path=" + vec_path + ", transpose=" + transpose);

		String [] args = new String[8];
		args[0] = mat_path;
		args[1] = vec_path;
		args[2] = out_path;
		args[3] = "" + nreducer;
		args[4] = edge_type;
		args[5] = "" + block_width;
		args[6] = "" + transpose;
		if( makesym == 1 )
			args[7] = "makesym";
		else
			args[7] = "nosym";

		ToolRunner.run(conf, new MatvecBlockDcache(), args);
		//System.out.println("Done MatvecNaiveSS. Output is saved in HDFS " + out_path);

		return;
	}




	/////////////////////////////////////////////////////////////////////////
	// Distributed Cache
	//
	public static void add_path_to_dc(Path inpath, Configuration conf)
	{
		try{
			FileSystem fs = FileSystem.get(conf);
			System.out.println("add_path_to_dc. URI=[" + inpath.toUri() + "]");
			FileStatus[] status = fs.listStatus(inpath);
			System.out.println("   number of files: " + status.length );

			for(int i = 0; i< status.length; i++) {
				Path cur_path = status[i].getPath();
				URI cur_uri = cur_path.toUri();
				System.out.println("  File = " + cur_path.getName() +", uri=" + cur_uri);
				DistributedCache.addCacheFile(cur_uri, conf);
			}
		} catch (IOException e) {
		}

	}

	/////////////////////////////////////////////////////////////////////////
	// Mappers and Reducers
	//

	// Identity Mapper
	public static class MapIdentity extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");

			if( tabpos > 0 ) {
				int out_key = Integer.parseInt(line_text.substring(0, tabpos));

				output.collect( new IntWritable(out_key) , new Text(line_text.substring(tabpos+1)) );
			} else {
				output.collect( new IntWritable(Integer.parseInt(line_text)) , new Text("") );
			}
		}
	}

	public static class MapIdentityLongText extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");

			if( tabpos > 0 ) {
				long out_key = Long.parseLong(line_text.substring(0, tabpos));

				output.collect( new LongWritable(out_key) , new Text(line_text.substring(tabpos+1)) );
			} else {
				output.collect( new LongWritable(Long.parseLong(line_text)) , new Text("") );
			}
		}
	}

	// Identity Mapper
	public static class MapIdentityDouble extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");

			int out_key = Integer.parseInt(line_text.substring(0, tabpos));

			output.collect( new IntWritable(out_key) , new DoubleWritable( Double.parseDouble(line_text.substring(tabpos+1)) ) );
		}
	}

	// Histogram Mapper
    public static class MapHistogram extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		long number_nodes = 0;
		int nreducers = 0;
		public void configure(JobConf job) {
			number_nodes = Long.parseLong(job.get("number_nodes"));
			nreducers = job.getNumReduceTasks();
			System.out.println("MapHistogram configure(): number_nodes = " + number_nodes + ", nreducers=" + nreducers);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");
			long first_column_key = 0;

			if( tabpos > 0 ) {
				String long_str= line_text.substring(0, tabpos);
				if(long_str.length() > 18 )
					return;
				first_column_key = Long.parseLong( long_str );
			} else {
				if(line_text.length() > 18 )
					return;

				first_column_key = Long.parseLong(line_text);
			}

			int out_key = (int)(first_column_key % nreducers);//int out_key = (int)((first_column_key/(double)number_nodes) * nreducers) ;
			//System.out.println("first_column_key = " + first_column_key + ", out_key=" + out_key);
			output.collect( new IntWritable(out_key) , new Text("") );
		}
    }

	// Histogram Mapper
    public static class MapHistogramText extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		long number_nodes = 0;
		int nreducers = 0;
		public void configure(JobConf job) {
			number_nodes = Long.parseLong(job.get("number_nodes"));
			nreducers = job.getNumReduceTasks();
			System.out.println("MapHistogram configure(): number_nodes = " + number_nodes + ", nreducers=" + nreducers);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");
			long first_column_key = 0;

			if( tabpos > 0 ) {
				String long_str= line_text.substring(0, tabpos);
				first_column_key = Math.abs(long_str.hashCode());
			} else {
				first_column_key = Math.abs(line_text.hashCode());
			}

			int out_key = (int)(first_column_key % nreducers);//int out_key = (int)((first_column_key/(double)number_nodes) * nreducers) ;
			output.collect( new IntWritable(out_key) , new Text("") );
		}
    }

	// Init Vector Generation Mapper
	public static class MapInitvectorGen extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, IntWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, IntWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");

			long out_key = Long.parseLong(line_text.substring(0, tabpos));

			output.collect( new LongWritable(out_key) , new IntWritable(1) );
		}
	}

	// Swap
	public static class MapSwapDouble extends MapReduceBase implements Mapper<LongWritable, Text, DoubleWritable, IntWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<DoubleWritable, IntWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");

			int out_val = Integer.parseInt(line_text.substring(0, tabpos));

			output.collect( new DoubleWritable( Double.parseDouble(line_text.substring(tabpos+1)) ), new IntWritable(out_val) );
		}
	}

	public static class MapMinMax extends MapReduceBase	implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments
				return;

			final String[] line = line_text.split("\t");
			double val;
			if( line[1].charAt(0) == 'v' )
				val = Double.parseDouble(line[1].substring(1));
			else
				val = Double.parseDouble(line[1]);
			output.collect( new IntWritable(0) , new DoubleWritable( val ) );
			output.collect( new IntWritable(1) , new DoubleWritable( val ) );
		}
	}

    public static class RedIdentity extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, Text>
    {
		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
        {
			while (values.hasNext()) {
				String cur_val = values.next().toString();
				output.collect( key, new Text( cur_val ) );
			}
		}
    }

    public static class RedIdentityGen<K1,V1> extends MapReduceBase implements Reducer<K1, V1, K1, V1>
    {
		public void reduce (final K1 key, final Iterator<V1> values, final OutputCollector<K1, V1> output, final Reporter reporter) throws IOException
        {
			while (values.hasNext()) {
				V1 cur_val = values.next();
				output.collect( key, cur_val );
			}
		}
    }

	// Histogram Reducer
    public static class RedHistogram<V1> extends MapReduceBase implements Reducer<IntWritable, V1, IntWritable, IntWritable>
    {
		int partition_no = -1;

		public void configure(JobConf job) {
			partition_no = job.getInt("mapred.task.partition", 0) ;
		}

		public void reduce (final IntWritable key, final Iterator<V1> values, final OutputCollector<IntWritable, IntWritable> output, final Reporter reporter) throws IOException
        {
			int count = 0;

			while (values.hasNext()) {
				values.next();
				count++;
			}

			output.collect( key, new IntWritable( count ) );
		}
    }

	// Sum Reducer (type: double)
    public static class RedSumDouble extends MapReduceBase implements Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable>
    {
		public void reduce (final IntWritable key, final Iterator<DoubleWritable> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;

			while (values.hasNext()) {
				double cur_val = values.next().get();
				sum += cur_val;
			}

			output.collect( key, new DoubleWritable( sum ) );
		}
    }

	// Sum Reducer (type: double)
    public static class RedSumLongDouble extends MapReduceBase implements Reducer<LongWritable, DoubleWritable, LongWritable, DoubleWritable>
    {
		public void reduce (final LongWritable key, final Iterator<DoubleWritable> values, final OutputCollector<LongWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;

			while (values.hasNext()) {
				double cur_val = values.next().get();
				sum += cur_val;
			}

			output.collect( key, new DoubleWritable( sum ) );
		}
    }

	// Sum Reducer (key: long, value: 'v' + val)
    public static class RedSumLongText extends MapReduceBase implements Reducer<LongWritable, Text, LongWritable, Text>
    {
		public void reduce (final LongWritable key, final Iterator<Text> values, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
        {
			long sum = 0;

			while (values.hasNext()) {
				String str_val = values.next().toString();
				long cur_val = Long.parseLong(str_val.substring(1));
				sum += cur_val;
			}

			output.collect( key, new Text( "v" + sum ) );
		}
    }

	// Sum Reducer (type: double)
    public static class RedSumDoubleTextKey extends MapReduceBase implements Reducer<Text, DoubleWritable, Text, DoubleWritable>
    {
		public void reduce (final Text key, final Iterator<DoubleWritable> values, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;

			while (values.hasNext()) {
				double cur_val = values.next().get();
				sum += cur_val;
			}

			output.collect( key, new DoubleWritable( sum ) );
		}
    }

    public static class RedSumDoubleLongKey extends MapReduceBase implements Reducer<LongWritable, DoubleWritable, LongWritable, DoubleWritable>
    {
		public void reduce (final LongWritable key, final Iterator<DoubleWritable> values, final OutputCollector<LongWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;

			while (values.hasNext()) {
				double cur_val = values.next().get();
				sum += cur_val;
			}

			output.collect( key, new DoubleWritable( sum ) );
		}
    }

    public static class RedAvgDouble extends MapReduceBase implements Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable>
    {
		public void reduce (final IntWritable key, final Iterator<DoubleWritable> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			double sum = 0;
			int count = 0;

			while (values.hasNext()) {
				double cur_val = values.next().get();
				sum += cur_val;
				count++;
			}

			output.collect( key, new DoubleWritable( sum/count ) );
		}
    }

    public static class RedMinMax extends MapReduceBase	implements Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable>
    {
		public void reduce (final IntWritable key, final Iterator<DoubleWritable> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			int i;
			double min_value = 1.0;
			double max_value = 0.0;

			int min_or_max = key.get();	// 0 : min, 1: max

			while (values.hasNext()) {
				double cur_value = values.next().get();

				//System.out.println("key = " + min_or_max +", val = " + cur_value);

				if( min_or_max == 0 ) {	// find min
					if( cur_value < min_value )
						min_value = cur_value;
				} else {				// find max
					if( cur_value > max_value )
						max_value = cur_value;
				}
			}

			if( min_or_max == 0)
				output.collect( key, new DoubleWritable(min_value) );
			else
				output.collect( key, new DoubleWritable(max_value) );
		}
    }

	// File operations

	public static void copyToLocalFile(Configuration conf, Path hdfs_path, Path local_path) throws Exception {
		FileSystem fs = FileSystem.get(conf);

		// read the result
		fs.copyToLocalFile(hdfs_path, local_path) ;
	}

	// read neighborhood number after each iteration.
	public static String readLocaldirOnelineString(String new_path) throws Exception
	{
		String output_path = new_path + "/part-00000";
		String str = "";
		try {
			BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( output_path ), "UTF8"));
			str = in.readLine();
			in.close();
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}

		if( str != null ) {
		    final String[] line = str.split("\t");

			return line[1];
		} else
			return "";
	}

	// read neighborhood number after each iteration.
	public static double readLocaldirOneline(String new_path) throws Exception
	{
		String output_path = new_path + "/part-00000";
		String str = "";
		try {
			BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( output_path ), "UTF8"));
			str = in.readLine();
			in.close();
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}

		if( str != null ) {
		    final String[] line = str.split("\t");

			return Double.parseDouble(line[1]);
		} else
			return 0;
	}

	// read neighborhood number after each iteration.
	public static double readLocaldirOneline(String new_path, int partno) throws Exception
	{
		String output_path = new_path;
		
		if( partno >= 0 && partno < 10 )
			output_path += "/part-0000" + partno;
		else if(partno >= 10 && partno < 100 )
			output_path += "/part-000" + partno;
		else
			output_path += "/part-00" + partno;

		String str = "";
		try {
			BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( output_path ), "UTF8"));
			str = in.readLine();
		} catch (UnsupportedEncodingException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}

		if( str != null ) {
		    final String[] line = str.split("\t");

			return Double.parseDouble(line[1]);
		} else
			return 0;
	}

	// read neighborhood number after each iteration.
	public static MinMaxInfo readMinMax(String new_path) throws Exception
	{
		MinMaxInfo info = new MinMaxInfo();
		String output_path = new_path + "/part-00000";
		String file_line = "";

		try {
			BufferedReader in = new BufferedReader(	new InputStreamReader(new FileInputStream( output_path ), "UTF8"));

			// Read first line
			file_line = in.readLine();

			// Read through file one line at time. Print line # and line
			while (file_line != null){
			    final String[] line = file_line.split("\t");

				if(line[0].startsWith("0")) 
					info.min = Double.parseDouble( line[1] );
				else
					info.max = Double.parseDouble( line[1] );

				file_line = in.readLine();
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return info;//result;
	}


	// used by RWR
	public static String loadQueryNodeInfo(String input_file) throws Exception
	{
		String cur_line = "";
		int query_count = 0;
		long []query_nodes = null;
		double []query_weights = null;
		double sum_weights = 0;
		String query_str = "";

		try {
			BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( input_file ), "UTF8"));
			
			while( cur_line != null ) {
				cur_line = in.readLine();
				if( cur_line.length() > 0 ) {
					String []tokens = cur_line.split("\t");
					query_nodes[query_count] = Long.parseLong(tokens[0]);
					query_weights[query_count] = Double.parseDouble(tokens[1]);
					
					sum_weights += query_weights[query_count];


					query_count++;
				}		
			}	
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}

		System.out.println("loadQueryNodeInfo: total " + query_count + " queries read.");

		// normalize
		for(int i = 0; i < query_count; i++) {
			query_weights[i] /= sum_weights;

			query_str += ("" + query_nodes[i] + " " + query_weights[i]);
			if( i != query_count - 1 )
				query_str += " ";
		}

		return query_str;
	}


	/////////////////////////////////////////////////////////////////////////
	// Linear Algebra related operations
	//

	// read L1 norm result
	public static double read_l1norm_result(Configuration conf) throws Exception
	{
		Path l2norm_output = new Path("l1norm_output");

		FileSystem lfs = FileSystem.getLocal(conf);
		// read the result
		String local_output_path = "l1norm";
		lfs.delete(new Path("l1norm/"), true);

		FileSystem fs = FileSystem.get(conf);
		fs.copyToLocalFile(l2norm_output, new Path(local_output_path) ) ;

		double result = PegasusUtils.readLocaldirOneline(local_output_path);

		lfs.delete(new Path("l1norm/"), true);//FileUtil.fullyDelete( fs.getLocal(conf), new Path(local_output_path));
		//FileUtil.fullyDelete( FileSystem.getLocal(conf), new Path("lanczos"));


		return result;
	}


	/////////////////////////////////////////////////////////////////////////
	// Partitioners
	//

	public static class RangePartition<V2> implements Partitioner<IntWritable, V2> {
		int number_nodes;
		public void configure(JobConf job) {
			number_nodes = Integer.parseInt(job.get("number_nodes"));
			System.out.println("RangePartition configure(): number_nodes = " + number_nodes);
		}

		// range partitioner
		public int getPartition(IntWritable key, V2 value, int numReduceTasks) {
			return (int)(( ((int)key.get())/(double)number_nodes) * numReduceTasks);
		}
	}

	public static class RangePartitionS1 implements Partitioner<IntWritable, Text> {
		int number_nodes;
		public void configure(JobConf job) {
			number_nodes = Integer.parseInt(job.get("number_nodes"));
			System.out.println("RangePartition configure(): number_nodes = " + number_nodes);
		}

		// range partitioner
		public int getPartition(IntWritable key, Text value, int numReduceTasks) {
			int result = (int)((key.get()/(double)number_nodes) * numReduceTasks);
			if( result == numReduceTasks )
				return (numReduceTasks-1);
			else
				return result;
		}
	}

	public static class IdentityPartition<V2> implements Partitioner<IntWritable, V2> {
		public void configure(JobConf job) {
		}

		// range partitioner
		public int getPartition(IntWritable key, V2 value, int numReduceTasks) {
			int cand_partition = key.get();
			if( cand_partition >= numReduceTasks ) 
				return numReduceTasks-1;

			return cand_partition;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// Etc
	//
	public static String partid_to_filename(int partid)
	{
		String filename = "";
		if( partid < 10 )
			filename = "part-0000" + partid ;
		else if(partid < 100 )
			filename = "part-000" + partid ;
		else if(partid < 1000 )
			filename = "part-00" + partid ;
		else if(partid < 10000 )
			filename = "part-0" + partid ;
		else
			filename = "part-" + partid ;

		return filename;
	}

}
