/*******************************************************************************
MatvecBlock.java - Block matrix-vector multiplication in hadoop.
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
Description: MatvecBlock is a hadoop program which performs matrix-vector multiplication using block-wise join.

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
import java.net.*;

class VectorElemDouble
{
	public short row;	
	public double val;
	public VectorElemDouble(short in_row, double in_val) {
		row = in_row;
		val = in_val;
	}
};

class BlockElemDouble
{
	public short row;	
	public short col;
	public double val;
	public BlockElemDouble(short in_row, short in_col, double in_val) {
		row = in_row;
		col = in_col;
		val = in_val;
	}
};

public class MatvecBlock extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // PASS 1: Hash join using Vector.BLOCKROWID == Matrix.BLOCKCOLID
	// vector: key=BLOCKID, value= (IN-BLOCK-INDEX VALUE)s
	// matrix: key=BLOCK-ROW		BLOCK-COL, value=(IN-BLOCK-ROW IN-BLOCK-COL VALUE)s
    //////////////////////////////////////////////////////////////////////
	public static class MapPass1 extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
    {
		int transpose = 0;

		public void configure(JobConf job) {
			transpose = Integer.parseInt(job.get("transpose"));
			String input_file = job.get("map.input.file");

			System.out.println("MatvecBlock.MapPass1: transpose = " + transpose);
			System.out.println("input_file = " + input_file);
		}


		public void map (final LongWritable key, final Text value, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");

			if( line.length == 2 ) {	// vector 
				output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1]) );
			} else {					// matrix
				if( transpose == 0 )
					output.collect( new IntWritable(Integer.parseInt(line[1])), new Text(line[0] + "\t" + line[2]) );
				else
					output.collect( new IntWritable(Integer.parseInt(line[0])), new Text(line[1] + "\t" + line[2]) );
			}
		}
	}



	// convert strVal to array of VectorElemDouble.
	// strVal is (ROW-ID   VALUE)s. ex) 0 0.5 1 0.3
	public static ArrayList<VectorElemDouble> parseVectorVal(String strVal) 
	{
		ArrayList arr = new ArrayList<VectorElemDouble>();
		final String[] tokens = strVal.split(" ");
		int i;

		for(i = 0; i < tokens.length; i += 2) {
			short row = Short.parseShort(tokens[i]);
			double val = Double.parseDouble(tokens[i+1]);
			arr.add( new VectorElemDouble(row, val) );
		}

		return arr;
	}

    public static class RedPass1 extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, Text>
    {
	    ArrayList<Integer> to_nodes_list = new ArrayList<Integer>();
	    ArrayList<Float> to_val_list = new ArrayList<Float>();
		protected int edge_type;
		protected int block_width;
		int transpose = 0;

		public void configure(JobConf job) {
			edge_type = Integer.parseInt(job.get("edge_type"));
			block_width = Integer.parseInt(job.get("block_width"));
			transpose = Integer.parseInt(job.get("transpose"));
			System.out.println("RedPass1 : configure is called. edge_type(0:REAL, 1:BINARY)= " + edge_type + ", block_width=" + block_width);
		}

		public void reduce (final IntWritable key, final Iterator<Text> values, final OutputCollector<IntWritable, Text> output, final Reporter reporter) throws IOException
        {
			int i;
			float vector_val = 0;

			to_nodes_list.clear();
			to_val_list.clear();
			ArrayList<VectorElemDouble> vectorArr = null;		// save vector
			ArrayList<ArrayList<BlockElemDouble>> blockArr = new ArrayList<ArrayList<BlockElemDouble>>();	// save blocks
			ArrayList<Integer> blockRowArr = new ArrayList<Integer>();	// save block rows(integer)

			while (values.hasNext()) {
				// vector: key=BLOCKID, value= (IN-BLOCK-INDEX VALUE)s
				// matrix: key=BLOCK-COL	BLOCK-ROW, value=(IN-BLOCK-COL IN-BLOCK-ROW VALUE)s
				String line_text = values.next().toString();
				final String[] line = line_text.split("\t");

				//System.out.println("[DEBUG] MatvecBlock.Reduce1 line_text=" + line_text);

				if( line.length == 1 ) {	// vector : VALUE
					vectorArr = parseVectorVal(line_text);
				} else {					// edge : ROWID		VALUE
					blockArr.add( parseBlockVal(line[1], edge_type, transpose) );
					int block_row = Integer.parseInt(line[0]);
					blockRowArr.add( block_row );
				}
			}

			int blockCount = blockArr.size();
			if( vectorArr == null || blockCount == 0 ) // missing vector or block.
				return;

			// Now we guarantee that there exists vector and at least one block.
			// For every matrix block, join it with vector and output partial results
			Iterator<ArrayList<BlockElemDouble>> blockArrIter = blockArr.iterator();
			Iterator<Integer> blockRowIter = blockRowArr.iterator();
			while( blockArrIter.hasNext() ){
				ArrayList<BlockElemDouble> cur_block = blockArrIter.next();
				int cur_block_row = blockRowIter.next();

				// multiply cur_block and vectorArr. 
				ArrayList<VectorElemDouble> cur_mult_result = multBlockVector( cur_block, vectorArr, block_width);
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
					output.collect(new IntWritable(cur_block_row), new Text(cur_block_output));
				}
			}
		}

    }

	// multiply one block and one vector
	// return : result vector
	public static ArrayList<VectorElemDouble> multBlockVector(ArrayList<BlockElemDouble> block, ArrayList<VectorElemDouble> vector, int i_block_width) 
	{
		int block_width = i_block_width;
		double[] out_vals = new double[i_block_width];	// buffer to save output
		short i;

		for(i=0; i < block_width; i++)		
			out_vals[i] = 0;

		Iterator<VectorElemDouble> vector_iter = vector.iterator();
		Iterator<BlockElemDouble> block_iter = block.iterator();
		BlockElemDouble saved_b_elem = null;

		while( vector_iter.hasNext() ) {
			VectorElemDouble v_elem = vector_iter.next();

			BlockElemDouble b_elem;
			while(block_iter.hasNext() || saved_b_elem != null) {
				if( saved_b_elem != null ) {
					b_elem = saved_b_elem;
					saved_b_elem = null;
				} else
					b_elem = block_iter.next();

				// compare v_elem.row and b_elem.col
				if( b_elem.col < v_elem.row )
					continue;
				else if( b_elem.col == v_elem.row ) {
					out_vals[ b_elem.row ] += b_elem.val * v_elem.val;
				} else {	// b_elem.col > v_elem.row
					saved_b_elem = b_elem;
					break;
				}
			}

		}
		
		ArrayList<VectorElemDouble> result_vector = null;
		for(i = 0; i < block_width; i++) {
			if( out_vals[i] != 0 ) {
				if( result_vector == null )
					result_vector = new ArrayList<VectorElemDouble>();
				result_vector.add( new VectorElemDouble(i, out_vals[i]) );
			}
		}

		return result_vector;
	}



	// convert strVal to array of BlockElemDouble.
	// strVal is (COL-ID     ROW-ID   VALUE)s. ex) 0 0 1 1 0 1 1 1 1
	// note the strVal is tranposed. So we should tranpose it to (ROW-ID   COL-ID ...) format.
	public static ArrayList<BlockElemDouble> parseBlockVal(String strVal, int i_edgetype, int transpose) 
	{
		ArrayList arr = new ArrayList<BlockElemDouble>();
		final String[] tokens = strVal.split(" ");
		int i;

		if( i_edgetype == EDGE_REAL ) {
			for(i = 0; i < tokens.length; i += 3) {
				short row = Short.parseShort(tokens[i+1]);
				short col = Short.parseShort(tokens[i]);
				double val = Double.parseDouble(tokens[i+2]);

				BlockElemDouble be;
				
				if( transpose == 0 )
					be = new BlockElemDouble(row, col, val);
				else
					be = new BlockElemDouble(col, row, val);
				arr.add( be );
			}
		} else {	// EDGE_BINARY
			for(i = 0; i < tokens.length; i += 2) {
				short row = Short.parseShort(tokens[i+1]);
				short col = Short.parseShort(tokens[i]);

				BlockElemDouble be;
				if( transpose == 0 )
					be = new BlockElemDouble(row, col, 1.0);
				else
					be = new BlockElemDouble(col, row, 1.0);
				arr.add( be );
			}
		}

		return arr;
	}

    //////////////////////////////////////////////////////////////////////
    // PASS 2: merge partial multiplication results
    //////////////////////////////////////////////////////////////////////
	public static class MapPass2 extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text>
    {
		public void map (final LongWritable key, final Text value, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
		{
			String line_text = value.toString();
			if (line_text.startsWith("#"))				// ignore comments in edge file
				return;

			final String[] line = line_text.split("\t");
			output.collect( new LongWritable(Long.parseLong(line[0])), new Text(line[1]) );
		}
	}

    public static class RedPass2 extends MapReduceBase
	implements Reducer<LongWritable, Text, LongWritable, Text>
    {
		protected int block_width;

		public void configure(JobConf job) {
			block_width = Integer.parseInt(job.get("block_width"));
			System.out.println("RedPass2 : configure is called. block_width=" + block_width);
		}

		public void reduce (final LongWritable key, final Iterator<Text> values, final OutputCollector<LongWritable, Text> output, final Reporter reporter) throws IOException
        {
			int i;
			double [] out_vals = new double[block_width];

			for(i=0; i < block_width; i++)
				out_vals[i] = 0;

			while (values.hasNext()) {
				String cur_str = values.next().toString();

				ArrayList<VectorElemDouble> cur_vector = parseVectorVal(cur_str);
				Iterator<VectorElemDouble> vector_iter = cur_vector.iterator();

				while( vector_iter.hasNext() ) {
					VectorElemDouble v_elem = vector_iter.next();
					out_vals[ v_elem.row ] += v_elem.val;
				}
			}

			String out_str = "";
			for(i = 0; i < block_width; i++) {
				if( out_vals[i] != 0 ) {
					if( out_str.length() > 0 )
						out_str += " ";
					out_str += ("" + i + " " + out_vals[i]) ;
				}
			}

			output.collect( key, new Text(out_str) );
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
	int transpose = 0;
	static int EDGE_REAL = 0, EDGE_BINARY = 1;
	protected int edge_type;
	protected int block_width = 32;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new MatvecBlock(), args);

		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("MatvecBlock <edge_path> <vector_path> <tempmv_path> <output_path> <# of reducer> <edge type: real or binary> <block width>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 8 ) {
			System.out.println("args.length=" + args.length);
			return printUsage();
		}

		edge_path = new Path(args[0]);
		if( args[1].compareTo("null") != 0 )
			vector_path = new Path(args[1]);
		tempmv_path = new Path(args[2]);
		output_path = new Path(args[3]);				
//		number_nodes = Integer.parseInt(args[4]);
		nreducer = Integer.parseInt(args[4]);

		if( args[5].compareTo("real") == 0 )
			edge_type = EDGE_REAL;
		else
			edge_type = EDGE_BINARY;

		block_width = Integer.parseInt(args[6]);
		transpose = Integer.parseInt(args[7]);


		System.out.println("Starting MatvecBlock. output_path=" + args[3] + ", transpose=" + transpose);


		final FileSystem fs = FileSystem.get(getConf());
		fs.delete(tempmv_path);
		fs.delete(output_path);

		// run job
		JobClient.runJob(configPass1());
		JobClient.runJob(configPass2());


		fs.delete(tempmv_path);

		return 0;
    }

	// Configure pass1
    protected JobConf configPass1 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecBlock.class);
//		conf.set("number_nodes", "" + number_nodes);
		conf.set("edge_type", "" + edge_type);
		conf.set("block_width", "" + block_width);
		conf.set("transpose", "" + transpose);

		conf.setJobName("MatvecBlock_pass1");
		
		conf.setMapperClass(MapPass1.class);        
		conf.setReducerClass(RedPass1.class);

		//conf.setInputPath(edge_path);
		//if( vector_path != null )
		//	conf.addInputPath(vector_path);
		//conf.setOutputPath(tempmv_path);
		if( vector_path != null )
			FileInputFormat.setInputPaths(conf, edge_path, vector_path);  
		else
			FileInputFormat.setInputPaths(conf, edge_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, tempmv_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);
		conf.setMapOutputValueClass(Text.class);

		return conf;
    }

	// Configure pass2
    protected JobConf configPass2 () throws Exception
    {
		final JobConf conf = new JobConf(getConf(), MatvecBlock.class);
//		conf.set("number_nodes", "" + number_nodes);
		conf.set("block_width", "" + block_width);

		conf.setJobName("MatvecBlock_pass2");
		
		conf.setMapperClass(MapPass2.class);        
		conf.setReducerClass(RedPass2.class);
		conf.setCombinerClass(RedPass2.class);

		//conf.setInputPath(tempmv_path);
		//conf.setOutputPath(output_path);
		FileInputFormat.setInputPaths(conf, tempmv_path);  
		conf.setOutputFormat(OverwriteOutputDirOutputFormat.class);
		FileOutputFormat.setOutputPath(conf, output_path);  

		conf.setNumReduceTasks( nreducer );

		conf.setOutputKeyClass(LongWritable.class);
		conf.setOutputValueClass(Text.class);

		return conf;
    }

}

