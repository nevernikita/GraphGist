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
File: InvVector.java
 - Invert each element of a vector
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

// x[i] = 1 / x[i]
public class InvVector extends Configured implements Tool 
{
	public static class MapStage1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, DoubleWritable>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<Text, DoubleWritable> output, final Reporter reporter) throws IOException
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
					output.collect(new Text(line_text.substring(0,tabpos)), new DoubleWritable(1.0/raw_val) );
			}
		}
	}

		
    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new InvVector(), args);

		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("InvVector <in_path> <out_path>");

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
		Path out_path = new Path(args[1]);

		System.out.println("\n-----===[PEGASUS: A Peta-Scale Graph Mining System]===-----\n");
		System.out.println("[PEGASUS] Computing InvVector. in_path=" + args[0] + ", out_path=" + args[1] + "\n");

		JobClient.runJob(configInvVector(in_path, out_path));

		System.out.println("\n[PEGASUS] InvVector computed. Output is saved in HDFS " + args[1] + "\n");

		return 0;
    }

	// Configure l2 norm
    protected JobConf configInvVector (Path in_path, Path out_path) throws Exception
    {
		final JobConf conf = new JobConf(getConf(), InvVector.class);
		conf.setJobName("InvVector");
		
		conf.setMapperClass(MapStage1.class);        

		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(out_path, true);

		FileInputFormat.setInputPaths(conf, in_path);  
		FileOutputFormat.setOutputPath(conf, out_path);  

		conf.setNumReduceTasks( 0 );

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}

