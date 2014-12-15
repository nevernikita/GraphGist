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
File: DotProduct.java
 - Compute dot product of two vectors.
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


public class DotProduct extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // STAGE 1: make initial pagerank vector
    //////////////////////////////////////////////////////////////////////

	// MapStage1: PegasusUtils.MapIdentity.class

	// RedStage1
    public static class RedStage1 extends MapReduceBase	implements Reducer<IntWritable, Text, IntWritable, Text>
    {
		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
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

			double result = val_double[0] * val_double[1];
			if( result != 0 )
				output.collect(new IntWritable(0), new Text("" + result));
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected int nreducers = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new DotProduct(), args);

		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("DotProduct <# of reducers> <y_path> <x_path> <a>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 3 ) {
			return printUsage();
		}

		nreducers = Integer.parseInt(args[0]);
		Path p1 = new Path(args[1]);
		Path p2 = new Path(args[2]);

		final FileSystem fs = FileSystem.get(getConf());
		System.out.println("\nRunning DotProduct... p1=" + p1.getName() + ", p2=" + p2.getName());

		Path dp_output1 = new Path("dp_output1");
		JobClient.runJob(configDotproduct1(nreducers, p1, p2, dp_output1));

		Path dp_output2 = new Path("dp_output2");
		JobClient.runJob(configDotproduct2(nreducers, dp_output1, dp_output2));

		return 0;
    }

	// Configure dot product 1/2
    protected JobConf configDotproduct1 (int nreducer, Path p1, Path p2, Path dp_output1) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DotProduct.class);
		conf.setJobName("DotProduct 1");
		
		conf.setMapperClass(PegasusUtils.MapIdentity.class);        
		conf.setReducerClass(DotProduct.RedStage1.class);

		FileSystem fs = FileSystem.get(getConf());
		fs.delete(dp_output1);

		FileInputFormat.setInputPaths(conf, p1, p2);  
		FileOutputFormat.setOutputPath(conf, dp_output1);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }

	// Configure dot product 2/2
    protected JobConf configDotproduct2 (int nreducer, Path dp_output1, Path dp_output2) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), DotProduct.class);
		conf.setJobName("DotProduct 2");
		
		conf.setMapperClass(PegasusUtils.MapIdentityDouble.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		FileSystem fs = FileSystem.get(getConf());
		fs.delete(dp_output2);

		FileInputFormat.setInputPaths(conf, dp_output1);  
		FileOutputFormat.setOutputPath(conf, dp_output2);  

		conf.setNumReduceTasks( 1 );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}

