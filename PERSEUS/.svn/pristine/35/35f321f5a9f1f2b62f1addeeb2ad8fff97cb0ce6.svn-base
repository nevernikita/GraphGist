/*******************************************************************************
MatvecBlockDcache.java - Block matrix-vector multiplication in hadoop.
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
Description: MatvecBlockDcache is a hadoop program which performs matrix-vector multiplication using block-wise join.

How to run: Use run_mvblock.sh to run the program in hadoop.	
	Usage) ./run_mvblock.sh [input HDFS edge path] [input_HDFS_vector_path or null] [output HDFS dir] [#_of_rows] [#_of_machines_in_hadoop] [edge_file] [edge type:real or binary] [block width] [1 or 0: copy_edgefile_or_not]

	(Arguments)
	[input HDFS edge path]: The HDFS directory where input edge is saved.
	[input_HDFS_vector_path or null]: The HDFS directory where input vector is saved. if edge and vector are saved in one HDFS directory, set this to null.
	[output HDFS dir]: The HDFS directory where result is saved.
	[#_of_rows]: Number of rows(or columns) in the input matrix( or vector). We assume the matrix with same number of rows and columns.
	[#_of_machines_in_hadoop]: number of machines to use in hadoop.
	[edge_file]: input matrix or vector file name. This name is used only for deciding log file name.
	[edge type]: Specify edge type(real or binary).
	[block width]: block width.
	[1 or 0: copy_edgefile_or_not]: If the input edge or vector file is in the local disk, set to 1. Then the local file will be copied to the input HDFS directory. If  the input edge or vector file is already in the input HDFS directory, set to 0.

Example: ./run_mvblock.sh mv_blockedge null 5 3 mv_input_5x5_block.txt real 32 1
*******************************************************************************/

package pegasus;

import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.filecache.*;
import java.net.*;

public class MatvecBlockDcache extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.BLOCKROWID == Matrix.BLOCKCOLID
	// vector: key=BLOCKID, value= (IN-BLOCK-INDEX VALUE)s
	// matrix: key=BLOCK-ROW		BLOCK-COL, value=(IN-BLOCK-ROW IN-BLOCK-COL VALUE)s
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text>
    {
        Map<Long, String> vector_map;// = new HashMap<Integer, Double>();
		protected int block_width;
		protected int edge_type;
		int transpose = 0;
		int makesym = 0;

		public void configure(JobConf job) {
			String right_vec_str = "";//job.get("right_vector");
			edge_type = Integer.parseInt(job.get("edge_type"));
			block_width = Integer.parseInt(job.get("block_width"));
			transpose = Integer.parseInt(job.get("transpose"));
			makesym = Integer.parseInt(job.get("makesym"));

			System.out.println("MatvecBlockDcache.MapPass1 Configure. Loading right vector...");
			vector_map = MatvecUtils.read_right_vector_block_dcache(job);
			System.out.println("Done.");
		}

		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			ArrayList<BlockElemDouble> edge_block = null;
			ArrayList<VectorElemDouble> vectorArr = null;

			final String[] tokens = line_text.split("\t");

			long dst_blk_id;
			if(transpose == 0 )
				dst_blk_id = Long.parseLong(tokens[1]);
			else
				dst_blk_id = Long.parseLong(tokens[0]);

			String vector_str = vector_map.get( dst_blk_id );
			if( vector_str != null ) {
				// extract matrix and the vector
				if( edge_block == null ) {
					edge_block = MatvecBlockSS.parseBlockVal(tokens[2], edge_type, transpose);
					vectorArr = MatvecBlock.parseVectorVal(vector_str);
				}

				// multiply cur_block and vectorArr. 
				ArrayList<VectorElemDouble> cur_mult_result = MatvecBlock.multBlockVector( edge_block, vectorArr, block_width);
				String cur_block_output = "";
				if( cur_mult_result != null && cur_mult_result.size() > 0 ) {
					Iterator<VectorElemDouble> cur_mult_result_iter = cur_mult_result.iterator();

					while( cur_mult_result_iter.hasNext() ) {
						VectorElemDouble elem = cur_mult_result_iter.next();
						if( cur_block_output != "" )
							cur_block_output += " ";
						cur_block_output += ("" + elem.row + " " + elem.val);
					}
					
					// output the partial result of multiplication.
					if( transpose == 0 ) 
						output.collect(new LongWritable(Long.parseLong(tokens[0])), new Text(cur_block_output));
					else
						output.collect(new LongWritable(Long.parseLong(tokens[1])), new Text(cur_block_output));
				}
			}

			if( makesym == 1 ) {
				if(transpose == 0 )
					dst_blk_id = Long.parseLong(tokens[0]);
				else
					dst_blk_id = Long.parseLong(tokens[1]);

				vector_str = vector_map.get( dst_blk_id );

				if( vector_str != null ) {
					// extract matrix and the vector
					edge_block = MatvecBlockSS.parseBlockVal(tokens[2], edge_type, 1 - transpose);

					// multiply cur_block and vectorArr. 
					ArrayList<VectorElemDouble> cur_mult_result = MatvecBlock.multBlockVector( edge_block, vectorArr, block_width);
					String cur_block_output = "";
					if( cur_mult_result != null && cur_mult_result.size() > 0 ) {
						Iterator<VectorElemDouble> cur_mult_result_iter = cur_mult_result.iterator();

						while( cur_mult_result_iter.hasNext() ) {
							VectorElemDouble elem = cur_mult_result_iter.next();
							if( cur_block_output != "" )
								cur_block_output += " ";
							cur_block_output += ("" + elem.row + " " + elem.val);
						}
						
						// output the partial result of multiplication.
						if( transpose == 0 ) 
							output.collect(new LongWritable(Long.parseLong(tokens[1])), new Text(cur_block_output));
						else
							output.collect(new LongWritable(Long.parseLong(tokens[0])), new Text(cur_block_output));
					}
				}
			}
		}
	}


    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
	protected Path vector_path = null;
    protected Path tempmv_path = null;
	protected Path output_path = null;
//	protected int number_nodes = 0;
	protected int nreducer = 1;
	static int EDGE_REAL = 0, EDGE_BINARY = 1;
	protected int edge_type;
	protected int block_width = 32;
	int transpose=0;
	int makesym = 0;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MatvecBlockDcache(), args);

		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MatvecBlockDcache <edge_path> <vector_path> <output_path> <# of reducer> <edge type: real or binary> <block width> <1 or 0: transpose>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 8 ) {
			return printUsage();
		}

		edge_path = new Path(args[0]);
		String right_vec_dcache_uri = args[1];
		String output_path_str = args[2];
		nreducer = Integer.parseInt(args[3]);

		if( args[4].compareTo("real") == 0 )
			edge_type = EDGE_REAL;
		else
			edge_type = EDGE_BINARY;

		block_width = Integer.parseInt(args[5]);
		transpose = Integer.parseInt(args[6]);
		if(args[7].equals("makesym"))
			makesym = 1;

		output_path = new Path(output_path_str);

		// run job
		System.out.println("Starting MatvecBlockDcache. block_width=" + block_width + ", HDFS edge_path=" + args[0] + ", local vector_path=" + args[1] + ", output_path=" + output_path_str + ",transpose=" + transpose + ", makesym = 1");


		// save the vector into distributed cache
		Path dcache_uri_path = new Path(right_vec_dcache_uri);
		PegasusUtils.add_path_to_dc( dcache_uri_path, getConf());

		System.out.println("Running MatvecBlockDcache...");
		JobClient.runJob( configPass1() );

		System.out.println("MatvecBlockDcache Done. Output is saved in HDFS " + output_path_str );

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecBlockDcache.class);
//		conf.set("number_nodes", "" + number_nodes);
		conf.set("edge_type", "" + edge_type);
		conf.set("block_width", "" + block_width);
		conf.set("transpose", "" + transpose);
		conf.set("makesym", "" + makesym);

		conf.setJobName("MatvecBlock_pass1");
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(MatvecBlock.RedPass2.class);
		conf.setCombinerClass(MatvecBlock.RedPass2.class);

		final FileSystem fs = FileSystem.get(conf);
		fs.delete(output_path);

		FileInputFormat.setInputPaths(conf, edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(LongWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }
}

