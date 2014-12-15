/*******************************************************************************
MatvecNaiveDcache.java - Naive matrix-vector multiplication in hadoop.
Copyright (c) 2009 U Kang(ukang@cs.cmu.edu)
All rights preserved

You may use this code without fee, for educational or research purposes.
You agree to cite the following paper whenever you use the code:
 *** John Tomson and Tom Johson: 
 *** "Graph mining for fun and profit", 
 *** Proc. ACM KDD, Las Vegas, Aug. 2008 

Any for-profit use requires the consent of the author(s).




Version: 0.10
Last modified: Mar 22, 2009
Description: MatvecNaiveDcache is a hadoop program which performs matrix-vector multiplication using element-wise join.

How to run: Use run_mvnaive.sh to run the program in hadoop.	
	Usage) ./run_mvnaive.sh [input HDFS dir] [output HDFS dir] [#_of_rows] [#_of_machines_in_hadoop] [edge_file] [1 or 0: copy_edgefile_or_not]

	(Arguments)
	[input HDFS dir]: The HDFS directory where input matrix or vectors are saved.
	[output HDFS dir]: The HDFS directory where where result is saved.
	[#_of_rows]: Number of rows(or columns) in the input matrix( or vector). We assume the matrix with same number of rows and columns.
	[#_of_machines_in_hadoop]: number of machines to use in hadoop.
	[edge_file]: input matrix or vector file name. This name is used only for deciding log file name.
	[1 or 0: copy_edgefile_or_not]: If the input edge or vector file is in the local disk, set to 1. Then the local file will be copied to the input HDFS directory. If  the input edge or vector file is already in the input HDFS directory, set to 0.

Example: ./run_mvnaive.sh mv_input mv_output 5 2 3 mv_input_5x5.txt 1
*******************************************************************************/

package pegasus;

//import Jama.*; 

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.filecache.*;

public class MatvecNaiveDcache extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.rowid == Matrix.colid
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
        Map<Integer, Double> vector_map = new HashMap<Integer, Double>();
		FileSystem fs;
		//Path[] localFiles;
		int transpose = 0;
		int makesym = 0;

/*
		// read right vector from string encoding.
		// The format of the string is <ROWID1 ROWID2 ROWID3 ...>
		protected int read_right_vector(String vec_str) {
			String [] tokens = vec_str.split("\t");
			
			for(int i = 0; i < tokens.length; i += 2) {
				int cur_key = Integer.parseInt(tokens[i]);
				double cur_val = 1.0;

				if( tokens[i+1].charAt(0) == 'v' )
					cur_val = Double.parseDouble(tokens[i+1].substring(1));
				else
					cur_val = Double.parseDouble(tokens[i+1]);

				vector_map.put(cur_key, cur_val);
			}

			return 0;
		}
*/
		public void configure(JobConf job) {
			String right_vec_str = "";//job.get("right_vector");
			//String dcache_uri = job.get("dcache_uri");
			//System.out.println("MatvecNaiveDcache.MapPass1 Configure. ");
			transpose = Integer.parseInt(job.get("transpose"));
			makesym = Integer.parseInt(job.get("makesym"));

			System.out.println("MatvecNaiveDcache.MapPass1 Configure. Loading right vector...");

			try {				
				fs = FileSystem.getLocal(new Configuration());
				Path[] localFiles = DistributedCache.getLocalCacheFiles(job);

				if( localFiles == null )
					return;

				System.out.println("Local Files # : " + localFiles.length );
				for(int i = 0; i < localFiles.length; i++) {
					System.out.println("\tPath[" + i + "]=" + localFiles[i].getName() );

					FSDataInputStream localFile = fs.open(localFiles[i]);

					BufferedReader in = new BufferedReader(new InputStreamReader(localFile, "UTF8"));

					String cur_str = "";
					while(cur_str != null) {
						cur_str = in.readLine();
						if(cur_str == null )
							break;

						String [] tokens = cur_str.split("\t");
						double cur_val = 1.0;
						if( tokens[1].charAt(0) == 'v' )
							cur_val = Double.parseDouble(tokens[1].substring(1));
						else
							cur_val = Double.parseDouble(tokens[1]);
					
						vector_map.put(Integer.parseInt(tokens[0]), cur_val );
					}

					localFile.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}


//			read_right_vector( right_vec_str );
			System.out.println("Done.");

//			System.out.println("right_vector=" + right_vec_str);


		}

		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, DoubleWritable> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			// line contains the edge, in <SRC> <DST> or <SRC> <DST> <VAL> format.
			final String[] line = line_text.split("\t");

			int dstid;
			if(transpose == 0 )
				dstid = Integer.parseInt(line[1]);
			else
				dstid = Integer.parseInt(line[0]);

			Double vector_val = vector_map.get( dstid );
			if( vector_val != null ) {
				double out_val = vector_val;
				if( line.length == 3 ) {
					if( line[2].charAt(0) == 'v' )
						out_val = Double.parseDouble(line[2].substring(1));
					else
						out_val = Double.parseDouble(line[2]);

					out_val *= vector_val.doubleValue();
				}

				if( transpose == 0 ) 
					output.collect( new IntWritable(Integer.parseInt(line[0])), new DoubleWritable(out_val) );
				else
					output.collect( new IntWritable(Integer.parseInt(line[1])), new DoubleWritable(out_val) );
			}

			if( makesym == 1 ) {
				int srcid;
				if(transpose == 0 )
					srcid = Integer.parseInt(line[0]);
				else
					srcid = Integer.parseInt(line[1]);

				vector_val = vector_map.get( srcid );
				if( vector_val != null ) {
					double out_val = vector_val;
					if( line.length == 3 ) {
						if( line[2].charAt(0) == 'v' )
							out_val = Double.parseDouble(line[2].substring(1));
						else
							out_val = Double.parseDouble(line[2]);

						out_val *= vector_val.doubleValue();
					}

					if( transpose == 0 ) 
						output.collect( new IntWritable(Integer.parseInt(line[1])), new DoubleWritable(out_val) );
					else
						output.collect( new IntWritable(Integer.parseInt(line[0])), new DoubleWritable(out_val) );
				}
			}
		}
	}


    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
//    protected Path tempmm_path = new Path("vav_tempmv_path");
	protected Path output_path;
//	protected int number_nodes = 0;
	protected int nreducers = 1;
	protected int transpose = 0;
//	protected String edge_file_name;
//	protected String job_name_base;
	protected String output_path_str;
	protected String dcache_uri;
	int makesym = 0;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MatvecNaiveDcache(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MatvecNaiveDcache <HDFS edge_path> <local vector_file> <HDFS output_path> <# of reducers> <1 or 0: tranpose or not>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 6 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);
		String right_vec_file = args[1];
		output_path_str = args[2];
		nreducers = Integer.parseInt(args[3]);
		transpose = Integer.parseInt(args[4]);
		if( args[5].startsWith("makesym") )
			makesym = 1;

		Path dcache_uri_path = new Path(right_vec_file);

		// add to distributed cache
		System.out.println("MatvecNaiveDcache. URI=[" + dcache_uri_path.toUri() + "]");
		FileSystem fs = FileSystem.get(getConf());
		FileStatus[] status = fs.listStatus(dcache_uri_path);
		System.out.println("   number of files: " + status.length );

		for(int i = 0; i< status.length; i++) {
			Path cur_path = status[i].getPath();
			URI cur_uri = cur_path.toUri();
			System.out.println("  File = " + cur_path.getName() +", uri=" + cur_uri);
			DistributedCache.addCacheFile(cur_uri, getConf());
		}

		// run job
		output_path = new Path(output_path_str);

		System.out.println("Running MatvecNaiveDcache... edge=[" + args[0] + "], output=[" + output_path_str+"]");
		JobClient.runJob( configPass1() );

		System.out.println("MatvecNaiveDcache Done. Output is saved in HDFS " + output_path_str );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecNaiveDcache.class);
		//conf.set("right_vector", "" + in_right_vec_str);
		//conf.set("dcache_uri", "" + dcache_uri);
		conf.set("makesym", ""+ makesym);
		conf.set("transpose", ""+ transpose);
		conf.setJobName("MatvecNaiveDcache_pass1");
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(PegasusUtils.RedSumDouble.class);
		conf.setCombinerClass(PegasusUtils.RedSumDouble.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(output_path);

		FileInputFormat.setInputPaths(conf, edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducers );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(DoubleWritable.class);

		return conf;
    }
}

