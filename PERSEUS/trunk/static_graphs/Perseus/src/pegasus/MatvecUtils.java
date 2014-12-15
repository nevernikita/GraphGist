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
File: MatvecUtils.java
 - Common utility functions for matrix-vector multiplication.
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
import org.apache.hadoop.filecache.*;

// common utility functions
public class MatvecUtils
{
	// convert Vector string to array of VectorElem.
	// strVal is (ROW-ID   VALUE)s. ex) 0 0.5 1 0.3
	public static double[] decodeBlockVector(String strVal, int block_width) 
	{
		int i;
		double [] vector = new double[block_width];
		for(i=0; i< block_width; i++)
			vector[i] = 0;

		//ArrayList arr = new ArrayList<VectorElem>();
		final String[] tokens = strVal.split(" ");


		for(i = 0; i < tokens.length; i += 2) {
			short row = Short.parseShort(tokens[i]);
			double val = Double.parseDouble(tokens[i+1]);
			
			vector[row] = val;
		}

		return vector;
	}


	// convert double[] to String
	// strVal is (ROW-ID   VALUE)s. ex) 0 0.5 1 0.3
	public static String encodeBlockVector(double[] vec, int block_width) 
	{
		int i;

		String result = "";

		for(i=0; i< block_width; i++) {
			if( vec[i] != 0 ) {
				if( result.length() > 0 )
					result += " ";

				result += ("" + i  + " " + vec[i]);
			}
		}

		return result;
	}

	// read filtering file information
	public static int[] read_filtering_result(Configuration conf) throws Exception
	{
		Path l1norm_output = new Path("matvecbzip_filter");

		FileSystem lfs = FileSystem.getLocal(conf);
		// read the result
		String local_output_path = "lanczos/mv_filter";
		lfs.delete(new Path("lanczos/mv_filter/"), true);
		lfs.delete(new Path("lanczos/"), true);

		FileSystem fs = FileSystem.get(conf);
		fs.copyToLocalFile(l1norm_output, new Path(local_output_path) ) ;

		String filter_files = PegasusUtils.readLocaldirOnelineString(local_output_path);

		if( filter_files.isEmpty() )
			return null;

		String[] tokens = filter_files.split(" ");

		lfs.delete(new Path("lanczos/mv_filter/"), true);
		lfs.delete(new Path("lanczos/"), true);//FileUtil.fullyDelete( fs.getLocal(conf), new Path(local_output_path));
		//FileUtil.fullyDelete( FileSystem.getLocal(conf), new Path("lanczos"));

		if( tokens.length > 0 ) {
			int[] result = new int[tokens.length];
			for(int i = 0; i < tokens.length; i++)
				result[i] = Integer.parseInt(tokens[i]);
		
			return result;
		}

		return null;
	}


	// read right vector
	public static Map<Long, Double> read_right_vector_dcache(JobConf job) {
		Map<Long, Double> vector_map = new HashMap<Long, Double>();

		try {				
			FileSystem fs = FileSystem.getLocal(new Configuration());
			Path[] localFiles = DistributedCache.getLocalCacheFiles(job);

			if( localFiles == null )
				return vector_map;

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
				
					vector_map.put(Long.parseLong(tokens[0]), cur_val );
				}

				localFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		return vector_map;
	}


	// read right vector from string encoding.
	// The format of the string is <ROWID1 VAL1 ROWID2 VAL2 ...>
	public static Map<Long, String> read_right_vector_block(String vec_str) {
		Map<Long, String> vector_map = new HashMap<Long, String>();
		String [] tokens = vec_str.split("\t");
		
		for(int i = 0; i < tokens.length; i += 2) {
			long cur_key = Long.parseLong(tokens[i]);

			vector_map.put(cur_key, tokens[i+1]);
		}

		return vector_map;
	}


	// read right vector from the distributed cache.
	// The format of each line is 
	public static Map<Long, String> read_right_vector_block_dcache(JobConf job) {
		Map<Long, String> vector_map = new HashMap<Long, String>();

		try {				
			FileSystem fs = FileSystem.getLocal(new Configuration());
			Path[] localFiles = DistributedCache.getLocalCacheFiles(job);
			
			if( localFiles == null )
				return vector_map; 

			System.out.println("Local Files # : " + localFiles.length );
			for(int i = 0; i < localFiles.length; i++) {
				System.out.println("\tPath[" + i + "]=" + localFiles[i].getName() );

				FSDataInputStream localFile = fs.open(localFiles[i]);

				BufferedReader in = new BufferedReader(
						new InputStreamReader(localFile, "UTF8"));

				//String right_vec_str = "";
				String cur_str = "";
				while(cur_str != null) {
					cur_str = in.readLine();
					if(cur_str == null )
						break;

					String[] tokens = cur_str.split("\t");
					vector_map.put(Long.parseLong(tokens[0]), tokens[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return vector_map;
	}
	public static Map<Long, int []> read_right_vectorind_block_dcache(JobConf job) {
		Map<Long, int []> vector_map = new HashMap<Long, int []>();

		try {				
			FileSystem fs = FileSystem.getLocal(new Configuration());
			Path[] localFiles = DistributedCache.getLocalCacheFiles(job);
			
			if( localFiles == null )
				return vector_map; 

			System.out.println("Local Files # : " + localFiles.length );
			for(int i = 0; i < localFiles.length; i++) {
				System.out.println("\tPath[" + i + "]=" + localFiles[i].getName() );

				FSDataInputStream localFile = fs.open(localFiles[i]);

				BufferedReader in = new BufferedReader(
						new InputStreamReader(localFile, "UTF8"));

				//String right_vec_str = "";
				String cur_str = "";
				while(cur_str != null) {
					cur_str = in.readLine();
					if(cur_str == null )
						break;

					String[] tokens = cur_str.split("\t");
					
					String [] in_row = tokens[1].split(" ");
					int[] in_row_int = new int[ in_row.length / 2 ];
					for(int j = 0; j < in_row_int.length; j++) 
						in_row_int[j] = Integer.parseInt(in_row[j*2]);

					vector_map.put(Long.parseLong(tokens[0]), in_row_int);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return vector_map;
	}

	public static int [] FindPartitions(Configuration conf, String vec_path_str, int number_nodes, int partition_k, String query_type) throws Exception {
		System.out.println("Running FindPartitions: vec_path=" + vec_path_str);

		String [] new_args = new String[5];
		new_args[0] = vec_path_str;
		new_args[1] = "matvecbzip_filter";
		new_args[2] = "" + number_nodes;
		new_args[3] = "" + partition_k;
		new_args[4] = query_type;
		ToolRunner.run(conf, new FindRelPartition(), new_args);

		// Read filtering information.
		int[] partitions = MatvecUtils.read_filtering_result(conf);
		if( partitions != null )
			for(int i = 0; i < partitions.length; i++) {
				System.out.println("\tpartitions[" + i + "] = " + partitions[i] );
			}

		return partitions;
	}
}
