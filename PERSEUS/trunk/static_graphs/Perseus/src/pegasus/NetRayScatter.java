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
File: NetRayScatter.java
 - Create the scatter plot for very large data
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

class MinMax_NetRay
{
	public double min;
	public double max;
};

public class NetRayScatter extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // STAGE 1: Find min/max of x and y values
	//  - Input: (x,y, [val])
	//  - Output: min(x), max(x), min(y), max(y)
    //////////////////////////////////////////////////////////////////////
	public static class MapStage1 extends MapReduceBase	implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		String field_ids = "";
		int [] field_id_arr = null;

		public void configure(JobConf job) {
			field_ids = job.get("field_ids");
			String[] token = field_ids.split(",");

			field_id_arr = new int[token.length];
			for(int i = 0; i < token.length; i++)
				field_id_arr[i] = Integer.parseInt(token[i]);

			System.out.println("MapStage1: field_ids = " + field_ids + ", length=" + token.length);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			double x_val = Double.parseDouble(line[ field_id_arr[0] ]);
			double y_val = Double.parseDouble(line[ field_id_arr[1] ]);
			
			output.collect( new IntWritable(0) , new DoubleWritable( x_val ) );	// min(x)
			output.collect( new IntWritable(1) , new DoubleWritable( x_val ) );	// max(x)

			output.collect( new IntWritable(2) , new DoubleWritable( y_val ) );	// min(y)
			output.collect( new IntWritable(3) , new DoubleWritable( y_val ) );	// max(y)
		}
	}

    public static class RedStage1 extends MapReduceBase	implements Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable>
    {
		public void configure(JobConf job) {
			System.out.println("RedStage2:");
		}

		public void reduce (final IntWritable key, final Iterator<DoubleWritable> values, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
        {
			boolean is_min_x_set = false;
			boolean is_max_x_set = false;
			boolean is_min_y_set = false;
			boolean is_max_y_set = false;
			double min_x = 0.0;
			double max_x = 0.0;
			double min_y = 0.0;
			double max_y = 0.0;

			int category = key.get();	// 0 : min_x, 1: max_x, 2: min_y, 3:max_y

			while (values.hasNext()) {
				double cur_value = values.next().get();

				if( category == 0 ) {		// min_x
					if( is_min_x_set == false ) {
						min_x = cur_value;
						is_min_x_set = true;
					} else {
						if( cur_value < min_x )
							min_x = cur_value;
					}
				} else if (category == 1) {	// max_x
					if( is_max_x_set == false ) {
						max_x = cur_value;
						is_max_x_set = true;
					} else {
						if( cur_value > max_x )
							max_x = cur_value;
					}
				} else if( category == 2 ) {// min_y
					if( is_min_y_set == false ) {
						min_y = cur_value;
						is_min_y_set = true;
					} else {
						if( cur_value < min_y )
							min_y = cur_value;
					}
				} else if (category == 3) {	// max_y
					if( is_max_y_set == false ) {
						max_y = cur_value;
						is_max_y_set = true;
					} else {
						if( cur_value > max_y )
							max_y = cur_value;
					}
				}
			}

			if( category == 0)
				output.collect( key, new DoubleWritable(min_x) );
			else if (category == 1)
				output.collect( key, new DoubleWritable(max_x) );
			else if (category == 2)
				output.collect( key, new DoubleWritable(min_y) );
			else if (category == 3)
				output.collect( key, new DoubleWritable(max_y) );

		}
    }


	// Digitize data
	public static class MapStage2 extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable>
    {
		double sm1_over_xmax_m_xmin = 0;
		double sm1_over_ymax_m_ymin = 0;
		double sm1_over_logxmax_m_logxmin = 0;
		double sm1_over_logymax_m_logymin = 0;

		double x_min = 0;
		double y_min = 0;

		int is_linear = 0;

		String field_ids = "";
		int [] field_id_arr = null;

		public void configure(JobConf job) {
			sm1_over_xmax_m_xmin = Double.parseDouble(job.get("sm1_over_xmax_m_xmin"));
			sm1_over_ymax_m_ymin = Double.parseDouble(job.get("sm1_over_ymax_m_ymin"));
			sm1_over_logxmax_m_logxmin = Double.parseDouble(job.get("sm1_over_logxmax_m_logxmin"));
			sm1_over_logymax_m_logymin = Double.parseDouble(job.get("sm1_over_logymax_m_logymin"));

			x_min = Double.parseDouble(job.get("x_min"));
			y_min = Double.parseDouble(job.get("y_min"));
			
			is_linear = Integer.parseInt(job.get("is_linear"));

			field_ids = job.get("field_ids");
			String[] token = field_ids.split(",");

			field_id_arr = new int[token.length];
			for(int i = 0; i < token.length; i++)
				field_id_arr[i] = Integer.parseInt(token[i]);

			System.out.println("MapStage1: sm1_over_xmax_m_xmin = " + sm1_over_xmax_m_xmin + ", sm1_over_ymax_m_ymin = " + sm1_over_ymax_m_ymin + ", x_min=" + x_min + ", y_min=" + y_min + ", is_linear = " + is_linear);
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<Text, LongWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			if(line.length < 2 )
				return;

			double cur_x = Double.parseDouble( line[ field_id_arr[0] ] );
			double cur_y = Double.parseDouble( line[ field_id_arr[1] ] );

			int block_cur_x = 0, block_cur_y = 0;


			if( is_linear == 1 ) {	// linear scale
				block_cur_x = (int)Math.ceil( (cur_x - x_min) * sm1_over_xmax_m_xmin + 0.5 );
				block_cur_y = (int)Math.ceil( (cur_y - y_min) * sm1_over_ymax_m_ymin + 0.5 );

				//System.out.println("block_cur_x = " + block_cur_x + ", block_cur_y = " + block_cur_y );
			} else {			// log scale
				block_cur_x = (int)Math.ceil( (Math.log(cur_x) - Math.log(x_min)) * sm1_over_logxmax_m_logxmin + 0.5 );
				block_cur_y = (int)Math.ceil( (Math.log(cur_y) - Math.log(y_min)) * sm1_over_logymax_m_logymin + 0.5 );

				//System.out.println("block_cur_x = " + block_cur_x + ", block_cur_y = " + block_cur_y );
			}

			long weight = 1;

			output.collect( new Text("" + block_cur_x + "\t" + block_cur_y), new LongWritable( weight ) );
		}

	}

    public static class RedStage2 extends MapReduceBase	implements Reducer<Text, LongWritable, Text, LongWritable>
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
    protected Path input_path = null;
	protected Path minmax_path = new Path("netray_minmax");
	protected Path output_path = null;
	protected int orig_nnodes = 0;
	protected int target_nnodes = 0;
	protected double sm1_over_xmax_m_xmin = 0;
	protected double sm1_over_ymax_m_ymin = 0;
	protected double sm1_over_logxmax_m_logxmin = 0;
	protected double sm1_over_logymax_m_logymin = 0;

	protected String field_ids = "";
	
	protected int nreducer = 1;
	protected int is_linear = 1;

	protected double x_min = 0;
	protected double x_max = 0;
	protected double y_min = 0;
	protected double y_max = 0;


    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new NetRayScatter(), args);

//		System.exit(result);
		return;
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("NetRayScatter <input_path> <output_path> <target number of nodes> <lin or log> <field_ids> <# of reducer> ");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 6 ) {
			return printUsage();
		}

		input_path = new Path(args[0]);
		output_path = new Path(args[1]);				
		target_nnodes = Integer.parseInt(args[2]);
		if( args[3].startsWith("log") )
			is_linear = 0;
		field_ids = args[4];
		nreducer = Integer.parseInt(args[5]);

		System.out.println("\n-----===[PEGASUS: A Peta-Scale Graph Mining System]===-----\n");
		System.out.println("[PEGASUS] NetRayScatter: Drawing a scatter plot. original nodes = " + orig_nnodes + ", target nnodes = " + target_nnodes + ", is_linear = " + is_linear + "\n");

		// Find min and max of data points
		JobClient.runJob(configStage1(input_path, minmax_path));

		final FileSystem fs = FileSystem.get(getConf());
		String local_output_path_str = "netray_lo";
		FileUtil.fullyDelete( FileSystem.getLocal(getConf()), new Path(local_output_path_str));
		String new_path = local_output_path_str + "/" ;
		fs.copyToLocalFile(minmax_path, new Path(new_path) ) ;
		readMinMax( new_path);

		sm1_over_xmax_m_xmin = (double) (target_nnodes - 1) / (double) (x_max - x_min);
		sm1_over_ymax_m_ymin = (double) (target_nnodes - 1) / (double) (y_max - y_min);
		sm1_over_logxmax_m_logxmin = (double) (target_nnodes - 1) / (double) (Math.log(x_max) - Math.log(x_min));
		sm1_over_logymax_m_logymin = (double) (target_nnodes - 1) / (double) (Math.log(y_max) - Math.log(y_min));

		// run job
		JobClient.runJob(configStage2());

		System.out.println("\n[PEGASUS] Scatter plot data generated.");
		System.out.println("[PEGASUS] Output is saved in the HDFS " + args[1] + "\n");
		System.out.println("x_min = " + x_min);
		System.out.println("x_max = " + x_max);
		System.out.println("y_min = " + y_min);
		System.out.println("y_max = " + y_max);

		return 0;
    }

	// read neighborhood number after each iteration.
	public void readMinMax(String new_path) throws Exception
	{
		MinMax_NetRay info = new MinMax_NetRay();
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
					x_min = Double.parseDouble( line[1] );
				else if(line[0].startsWith("1")) 
					x_max = Double.parseDouble( line[1] );
				else if(line[0].startsWith("2")) 
					y_min = Double.parseDouble( line[1] );
				else if(line[0].startsWith("3")) 
					y_max = Double.parseDouble( line[1] );

				file_line = in.readLine();
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		return;
	}


	// Configure stage 1
    protected JobConf configStage1 (Path in_path, Path out_path) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), NetRayScatter.class);
		conf.set("field_ids", field_ids);		
		conf.setJobName("NetRayScatter_Stage1");
		
		conf.setMapperClass(MapStage1.class);        
		conf.setReducerClass(RedStage1.class);
		conf.setCombinerClass(RedStage1.class);

		FileInputFormat.setInputPaths(conf, in_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( 1 );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }

	// Configure stage 2
    protected JobConf configStage2 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), NetRayScatter.class);
		conf.set("sm1_over_xmax_m_xmin", "" + sm1_over_xmax_m_xmin);		
		conf.set("sm1_over_ymax_m_ymin", "" + sm1_over_ymax_m_ymin);	
		conf.set("sm1_over_logxmax_m_logxmin", "" + sm1_over_logxmax_m_logxmin);		
		conf.set("sm1_over_logymax_m_logymin", "" + sm1_over_logymax_m_logymin);	
		conf.set("field_ids", field_ids);		

		conf.set("x_min", "" + x_min);		
		conf.set("y_min", "" + y_min);		

		conf.set("is_linear", "" + is_linear);		
		conf.setJobName("NetRayScatter_Stage2");
		
		conf.setMapperClass(MapStage2.class);        
		conf.setReducerClass(RedStage2.class);
		conf.setCombinerClass(RedStage2.class);

		FileSystem fs = FileSystem.get(getConf());
		fs.delete(output_path, true);

		FileInputFormat.setInputPaths(conf, input_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		int num_reduce_tasks = nreducer;

		conf.setNumReduceTasks( num_reduce_tasks );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(LongWritable.class);

		return conf;
    }
}

