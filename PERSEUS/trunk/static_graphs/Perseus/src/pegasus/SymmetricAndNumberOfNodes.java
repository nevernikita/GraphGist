/***********************************************************************
    PERSEUS: Understanding Peta-Scale Graphs
    Authors: Danai Koutra
    
 (based on PEGASUS - authors: Christos Faloutsos, Duen Horng Chau, Christos Faloutsos)

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
File: SymmetricAndNumberOfNodes.java
 - Check if the original graph is symmetric and find the total number of nodes.
Version: 2.0
***********************************************************************/
package pegasus;

import java.io.*;
import java.util.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class SymmetricAndNumberOfNodes extends Configured implements Tool 
{
    /////////////////////////////////////////////////////////////////////////////////
    // Check if the original graph is symmetric and find the total number of nodes.
	//  - Input: edge file
	//  - Output: is_symmetric_OR_not and total number of nodes
    ////////////////////////////////////////////////////////////////////////////////
	public static class MapStage1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>
    {

		public void map (final LongWritable key, final Text value, final OutputCollector<Text, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			if(line.length < 2 )						// ignore ill-formated data.
				return;

			/* STEP 1: Original Graph 
			 * Emit the original edges 
			 */
			output.collect( new Text("original"), new Text(line_text) );
			
			/* STEP 2: Symmetric Graph
			 * 1. Emit the original edges.
			 * 2. Create the reverse edges.
			 * 3. Emit the reverse edges.
			 */
			output.collect( new Text("symmetric"), new Text(line_text) );
			String reverse_line_text = new String( line[1] + "\t" + line[0] );
			output.collect( new Text("symmetric"), new Text(reverse_line_text) );
			
			/* STEP 3: Find total number of nodes
			 * 1. For each edge, find the max nodeID.
			 * 2. Emit the max nodeID.
			 */
			long max = Math.max(Long.parseLong(line[0]), Long.parseLong(line[1]));
			output.collect( new Text("max"), new Text(Long.toString(max)));
			
		}
	}
	
	public static class CombStage1 extends MapReduceBase implements Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException 
		{
			/* removing the duplicates */
			Set<Text> uniques = new HashSet<Text>();
		    while ( values.hasNext() ){ 
		    	Text value = values.next();
		    	if (uniques.add(value))
		    		output.collect(key, value);
		     }
			
		}
	}
	

    public static class RedStage1 extends MapReduceBase	implements Reducer<Text, Text, Text, Text>
    {
		public void reduce (final Text key, final Iterator<Text> values, final OutputCollector<Text, Text> output, final Reporter reporter) throws IOException
        {			
			
			if (key.toString().equalsIgnoreCase("max")){
				long max = 0;
				long curr = 0;
				while (values.hasNext()) {
					curr = Long.parseLong(values.next().toString());
					if ( curr > max ) 
						max = curr;
				}	
				
				// +1 because the nodes begin from 0
				output.collect(key, new Text(Long.toString(max+1)));
				
//				nodesNo = max + 1;
			}
			
			if ( key.toString().equalsIgnoreCase("original") || key.toString().equalsIgnoreCase("symmetric") ){
				long edges_no = 0;
				while (values.hasNext()){
					values.next();
					edges_no++;
				}

				output.collect(key, new Text(Long.toString(edges_no)));
				
			}
		}
    }

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected Path output_path = null;
	protected Path edge_path = null;
	protected int nreducers = 1;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {  	
		ToolRunner.run(new Configuration(), new SymmetricAndNumberOfNodes(), args);

		return;
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("SymmetricAndNumberOfNodes <edge_path> <output_path> <# of reducers> ");

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

		edge_path = new Path(args[0]);
		output_path = new Path(args[1]);				
		nreducers = Integer.parseInt(args[2]);

		System.out.println("\n-----===[PEGASUS: A Peta-Scale Graph Mining System]===-----\n");
		System.out.println("[PEGASUS] Mining info about the graph: symmetric? total number of nodes?.\n");

		JobClient.runJob(configStage1());

		FileSystem fs = FileSystem.get( getConf() );
		fs.copyToLocalFile(output_path, new Path(output_path+"_local"));
		
		System.out.println("\n[PEGASUS] Information retrieved.");
		System.out.println("[PEGASUS] Information saved in " + args[1] + "\n");

		return 0;
    }

    // Configure pass1
    @SuppressWarnings("deprecation")
	protected JobConf configStage1() throws Exception
    {
		final JobConf conf = new JobConf(getConf(), SymmetricAndNumberOfNodes.class);
		conf.setJobName("Getting_Graph_Info");
		
		conf.setMapperClass(MapStage1.class);
		conf.setCombinerClass(CombStage1.class);
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

