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
File: HadamardProduct.java
 - Compute Hadamard product of two matrices.
Version: 3.0
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

// HadamardProduct main class
public class HadamardProduct extends Configured implements Tool 
{
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, DoubleWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			int tabpos = line_text.indexOf("\t");
			double raw_val = 0;

			if( tabpos > 0 ) {
				if( line_text.charAt(tabpos+1) == 'v') {
					raw_val = Double.parseDouble(line_text.substring(tabpos+2));
				} else {
					raw_val = Double.parseDouble(line_text.substring(tabpos+1));
				}
				
				if( raw_val != 0 )
					output.collect(new LongWritable(Long.parseLong(line_text.substring(0,tabpos))), new DoubleWritable(raw_val) );
			}
		}
	}


    public static class RedPass1 extends MapReduceBase	implements Reducer<LongWritable, DoubleWritable, LongWritable, Text>
    {
		public void reduce (final LongWritable key, final Iterator<DoubleWritable> values, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
        {
			int i = 0;
			double val_double[] = new double[2];
			val_double[0] = 0;
			val_double[1] = 0;

			while (values.hasNext()) {
				String cur_value_str = values.next().toString();
				if( cur_value_str.charAt(0) == 'v')
					val_double[i] = Double.parseDouble( cur_value_str.substring(1) );
				else
					val_double[i] = Double.parseDouble( cur_value_str );

				i++;
			}

			if( i == 2 ) {
				double result = val_double[0] * val_double[1];	// Hadamard product
				if( result != 0 )
					output.collect(key, new Text("v" + result));
			}

		}
    }
	
    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	Path in_path1;
	Path in_path2;
	Path out_path;
	int nreducer = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new HadamardProduct(), args);

		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("HadamardProduct <in_path1> <in_path2> <out_path> <nreducer>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		in_path1 = new Path(args[0]);
		in_path2 = new Path(args[1]);
		out_path = new Path(args[2]);
		nreducer = Integer.parseInt(args[3]);
		
		System.out.println("\n-----===[PEGASUS: A Peta-Scale Graph Mining System]===-----\n");
		System.out.println("[PEGASUS] Computing HadamardProduct. in_path1=" + args[0] + "in_path2=" + args[1] + ", out_path=" + args[2] + "\n");

		JobClient.runJob(configHadamardProduct());

		System.out.println("\n[PEGASUS] HadamardProduct computed. Output is saved in HDFS " + args[2] + "\n");

		return 0;
    }

	// Configure HadamardProduct
    protected JobConf configHadamardProduct () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), HadamardProduct.class);
		conf.setJobName("HadamardProduct");
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);        

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(out_path, true);

		FileInputFormat.setInputPaths(conf, in_path1, in_path2);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(LongWritable.class);
		conf.setMapOutputValueClass(DoubleWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }
}

