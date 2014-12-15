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
File: LinearAlgebraUtils.java
 - Utilities for linear algebra operations
Version: 3.0
***********************************************************************/

package pegasus;

import Jama.*; 
import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

class alpha_beta
{
	public double [] alpha;
	public double [] beta;
};

// Common functions for LinearAlgebra
public class LinearAlgebraUtils
{
	public static double epsilon = 1e-13;

	// read L2 norm result
	public static double read_l2norm_result(Configuration conf) throws Exception
	{
		Path l2norm_output = new Path("l2norm_output");

		FileSystem lfs = FileSystem.getLocal(conf);
		// read the result
		String local_output_path = "lanczos/l2norm";
		lfs.delete(new Path("lanczos/l2norm/"), true);
		lfs.delete(new Path("lanczos/"), true);

		FileSystem fs = FileSystem.get(conf);
		fs.copyToLocalFile(l2norm_output, new Path(local_output_path) ) ;

		double before_sqrt = PegasusUtils.readLocaldirOneline(local_output_path);
		double result = Math.sqrt( before_sqrt );

		lfs.delete(new Path("lanczos/l2norm/"), true);
		lfs.delete(new Path("lanczos/"), true);

		return result;
	}

	// read L1 norm result
	public static double read_l1norm_result(Configuration conf) throws Exception
	{
		Path l1norm_output = new Path("l1norm_output");

		FileSystem lfs = FileSystem.getLocal(conf);
		// read the result
		String local_output_path = "lanczos/l1norm";
		lfs.delete(new Path("lanczos/l1norm/"), true);
		lfs.delete(new Path("lanczos/"), true);

		FileSystem fs = FileSystem.get(conf);
		fs.copyToLocalFile(l1norm_output, new Path(local_output_path) ) ;

		double before_sqrt = PegasusUtils.readLocaldirOneline(local_output_path);
		double result = Math.sqrt( before_sqrt );

		lfs.delete(new Path("lanczos/l1norm/"), true);
		lfs.delete(new Path("lanczos/"), true);

		return result;
	}

	// Load alphas and betas from the Lanczos output
	public static alpha_beta load_alpha_beta(int m) throws IOException
	{
		int i;
		String file_name = "lanczos.ab";

		alpha_beta ab = new alpha_beta();
		ab.alpha = new double[m+1];
		ab.beta = new double[m+1];

		System.out.println("Loading alpha and beta from " + file_name + "...");

		// load matrix_file into Tm
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( file_name ), "UTF8"));

		String cur_str = "";
		int index = 0;
		int count = 0;
		while(cur_str != null) {
			cur_str = in.readLine();
			if(cur_str == null )
				break;

			String[] tokens = cur_str.split("\t");
			index = Integer.parseInt(tokens[0]);
			ab.alpha[index] = Double.parseDouble(tokens[1]);
			ab.beta[index] = Double.parseDouble(tokens[2]);

			if(++count >= m )
				break;
		}

		System.out.println("Done. Read alpha and beta up to " + index + ".");

		return ab;
	}

	// construct the tridiagonal matrix from alpha and beta
	public static Matrix getT(int nrow, double[] alpha, double[] beta) {
		int i;

		Matrix Tm = new Matrix(nrow, nrow); 
		for(i=0; i<nrow; i++) {
			Tm.set(i, i, alpha[i+1]);
			if( (i-1) >= 0 )
				Tm.set(i, i-1, beta[i]);
			if( (i+1) < nrow )
				Tm.set(i, i+1, beta[i+1]);
		}

		return Tm;
	}

	// comparator using descending absolute values
	public static Comparator<Double> getAbsDoubleDscComparator(){
		return new Comparator<Double>(){
			public int compare(Double o1,
							   Double o2){
				return -1 * Double.compare(Math.abs(o1), Math.abs(o2));
			}
		};
	}

	// comparator using ascending absolute values
	public static Comparator<Double> getAbsDoubleAscComparator(){
		return new Comparator<Double>(){
			public int compare(Double o1,
							   Double o2){
				return Double.compare(Math.abs(o1), Math.abs(o2));
			}
		};
	}

	// comparator using descending values
	public static Comparator<Double> getDoubleDscComparator(){
		return new Comparator<Double>(){
			public int compare(Double o1,
							   Double o2){
				return -1 * Double.compare(o1, o2);
			}
		};
	}

	// save matrix M[1:nrow, 1:ncol] to disk, 1 row per line
	public static void save_matrix_to_disk( Matrix M, int nrow, int ncol, String out_filename) throws Exception {
		FileWriter file = new FileWriter(out_filename);
		BufferedWriter out = new BufferedWriter (file);
		
		int i,j;
		for(i=0; i < nrow; i++)	{
			for(j = 0; j < ncol; j++) {
				out.write("" + M.get(i,j) );
				if( j != (ncol-1) ) 
					out.write("\t");
				else
					out.write("\n");
			}
		}
		out.close();
	}

	// save matrix M[1:nrow, 1:ncol] to disk, element per line
	public static void save_matrix_all_to_disk_sparse( Matrix M, int nrow, int ncol, String out_filename) throws Exception {
		FileWriter file = new FileWriter(out_filename);
		BufferedWriter out = new BufferedWriter (file);
		
		int i,j;
		for(i=0; i < nrow; i++)	{
			for(j = 0; j < ncol; j++) {
				out.write("" + i + "\t" + j + "\t" + M.get(i,j) + "\n");
			}
		}
		out.close();
	}

	// save vector M[:, col] to disk. notice that col_index starts from 0.
	public static void save_vector_to_disk( Matrix M, int nrow, int col_index, String out_filename) throws Exception {

		System.out.println("save_vector_to_disk: nrow=" + nrow + ", col_index=" + col_index);
		FileWriter file = new FileWriter(out_filename);
		BufferedWriter out = new BufferedWriter (file);
		
		int i,j;
		for(i=0; i < nrow; i++)	{
			out.write("" + M.get(i,col_index) );
			if( i != (nrow-1) ) 
				out.write("\t");
			else
				out.write("\n");
		}
		out.close();
	}

	// save vector M[:, col] to disk. notice that col_index starts from 0.
	public static void save_vector_to_disk_2col( Matrix M, int nrow, int col_index, String out_filename) throws Exception {
		FileWriter file = new FileWriter(out_filename);
		BufferedWriter out = new BufferedWriter (file);
		
		int i,j;
		for(i=0; i < nrow; i++)	{
			out.write(i + "\tv" + M.get(i,col_index) + "\n");
		}
		out.close();
	}


	////////////////////////////////////////////////////////////////
	// Linear Algebra Operations
	//

	// distributed cache matrix-vector multiplication. The matrix is given in a path.
	public static Path MatvecCache(Configuration conf, int m, Path edge_path, int nreducer, String vec_filename) throws Exception{
		System.out.println("Running MatvecCache: m=" + m + ", vec_file=" + vec_filename);
		String [] args = new String[4];
		args[0] = new String( "" + edge_path);
		args[1] = new String("" + nreducer);
		args[2] = new String( vec_filename );
		args[3] = new String("" + m);
		ToolRunner.run(conf, new MatvecCache(), args);
		System.out.println("Done MatvecCache: m=" + m + ", vec_file=" + vec_filename);

		return (new Path("matvec_out_path"));
	}

	// distributed cache matrix-vector multiplication. The matrix is given in multiple paths.
	public static Path VecarrVecCache(Configuration conf, String path_prefix, int m, int nreducer, String vec_filename) throws Exception{
		System.out.println("Running VecarrVecCache: path_prefix=" + path_prefix + ", m=" + m + ", vec_file=" + vec_filename);
		String [] args = new String[5];
		args[0] = new String(path_prefix);
		args[1] = new String( "" + m);
		args[2] = new String("" + nreducer);
		args[3] = new String( vec_filename );
		args[4] = new String("" + m);
		ToolRunner.run(conf, new VecarrvecCache(), args);
		System.out.println("Done VecarrVecCache: m=" + m + ", vec_file=" + vec_filename);

		return (new Path("vav_matvec_out_path"));
	}

	// used in eigentriangle
	public static Path VecarrVecEigen(Configuration conf, int m, int nreducer, String vec_filename, String base_path_name) throws Exception{
		System.out.println("Running VecarrVecCache: m=" + m + ", vec_file=" + vec_filename);
		String [] args = new String[5];
		args[0] = new String( "" + m);
		args[1] = new String("" + nreducer);
		args[2] = new String( vec_filename );
		args[3] = new String("" + m);
		args[4] = base_path_name;
		ToolRunner.run(conf, new VecarrvecEigen(), args);
		System.out.println("Done VecarrvecEigen: m=" + m + ", vec_file=" + vec_filename);

		return (new Path("vav_matvec_out_path"));
	}

	// computes dot product of two vectors
	public static double DotProduct(Configuration conf, int nreducer, String p1_str, String p2_str) throws Exception{
		System.out.println("Running DotProduct: p1=" + p1_str + ", p2=" + p2_str);
		String [] args = new String[3];
		args[0] = new String("" + nreducer);
		args[1] = new String( p1_str );
		args[2] = new String( p2_str );
		ToolRunner.run(conf, new DotProduct(), args);
		System.out.println("Done DotProduct: p1=" + p1_str + ", p2=" + p2_str);

		String local_output_path = "lanczos/dp";
		Path dp_output2 = new Path("dp_output2");

		FileSystem lfs = FileSystem.getLocal(conf);
		lfs.delete(new Path("lanczos/dp"), true);

		FileSystem fs = FileSystem.get(conf);
		fs.copyToLocalFile(dp_output2, new Path(local_output_path) ) ;
		double result = PegasusUtils.readLocaldirOneline(local_output_path);
		
		lfs.delete(new Path("lanczos/dp"), true);

		return result;
	}

	// y = y + ax
	public static Path Saxpy(Configuration conf, int nreducer, Path py, Path px, double a) throws Exception{
		System.out.println("Running Saxpy: py=" + py.getName() + ", px=" + px.getName() + ", a=" +a);

		String [] args = new String[4];
		args[0] = new String("" + nreducer);
		args[1] = new String(py.getName());
		args[2] = new String(px.getName() );
		args[3] = new String("" + a);
		int saxpy_result = ToolRunner.run(conf, new Saxpy(), args);

		FileSystem fs = FileSystem.get(conf);
		fs.delete(py, true);

		if( saxpy_result == 1 )
			fs.rename(new Path("saxpy_output1"), py );
		else
			fs.rename(new Path("saxpy_output"), py );
		
		return py;
	}

	// ||y||_2
	public static double L2norm(Configuration conf, int nreducer, Path px) throws Exception{
		System.out.println("Running L2norm: px=" + px.getName());

		String [] args = new String[1];
		args[0] = new String("" + px.getName());

		ToolRunner.run(conf, new L2norm(), args);
		double result = LinearAlgebraUtils.read_l2norm_result(conf);
		
		return result;
	}

	// ||y||_1
	public static double L1norm(Configuration conf, int nreducer, String in_path_str) throws Exception{
		System.out.println("Running L2norm: path=" + in_path_str);

		String [] args = new String[1];
		args[0] = new String("" + in_path_str);

		ToolRunner.run(conf, new L1norm(), args);
		double result = LinearAlgebraUtils.read_l1norm_result(conf);
		FileSystem lfs = FileSystem.getLocal(conf);
		lfs.delete(new Path("l1norm"), true);
		
		return result;
	}

	// y = s * y
	public static Path ScalarMult(Configuration conf, Path py, double s) throws Exception{
		System.out.println("Running ScalarMult: py=" + py.getName() + ", s=" +s);

		String [] args = new String[2];
		args[0] = new String(py.getName());
		args[1] = new String(""+s );

		ToolRunner.run(conf, new ScalarMult(), args);

		FileSystem fs = FileSystem.get(conf);
		fs.delete(py, true);

		fs.rename(new Path("smult_output"), py );
		
		return py;
	}

	// y = s * y
	public static Path ScalarMult(Configuration conf, Path p_in, Path p_out, double s) throws Exception{
		System.out.println("Running ScalarMult: p_in=" + p_in.getName() + ", s=" +s);

		String [] args = new String[2];
		args[0] = new String(p_in.getName());
		args[1] = new String(""+s );

		ToolRunner.run(conf, new ScalarMult(), args);

		FileSystem fs = FileSystem.get(conf);
		fs.delete(p_out, true);

		fs.rename(new Path("smult_output"), p_out );
		
		return p_out;
	}

	// inverse vector
	public static void InvVector(Configuration conf, String in_path_str, String out_path_str) throws Exception{
		System.out.println("Running InvVector: in_path=" + in_path_str + ", out_path_str=" +out_path_str);

		String [] args = new String[2];
		args[0] = in_path_str;
		args[1] = out_path_str;

		ToolRunner.run(conf, new InvVector(), args);
	}

	// hadamard product 
	public static void HadamardProduct(Configuration conf, int nreducer, String in_path1_str, String in_path2_str, String out_path_str) throws Exception{
		System.out.println("Running InvVector: in_path1_str=" + in_path1_str + "in_path2_str=" + in_path2_str + ", out_path_str=" +out_path_str);

		String [] args = new String[4];
		args[0] = in_path1_str;
		args[1] = in_path2_str;
		args[2] = out_path_str;
		args[3] = "" + nreducer;

		ToolRunner.run(conf, new HadamardProduct(), args);
	}

	// normalize vector
	public static void NormalizeVector(Configuration conf, int nreducer, String in_path_str, String out_path_str) throws Exception {

		String []new_args = new String[4];
		new_args[0] = in_path_str;
		new_args[1] = out_path_str;
		new_args[2] = "" + nreducer;
		new_args[3] = "" + 1;
		ToolRunner.run(conf, new NormalizeVector(), new_args);
	}

	/////////////////////////////////////////////////////////////////////////////////
	// Run block-version tools

	public static Path MvPrep(Configuration conf, Path edge_path, int number_nodes, int block_size, int nreducer, String out_prefix, int makesym) throws Exception{
		System.out.println("Running MvPrep: edge_path=" + edge_path.getName() );

		String [] args = new String[8];
		args[0] = new String("" + edge_path);
		args[1] = new String("mvprep_out");
		args[2] = new String("" + number_nodes);
		args[3] = new String("" + block_size);
		args[4] = new String("" + nreducer);
		args[5] = new String(edge_path.getName());
		args[6] = out_prefix;
		if( makesym == 1 )
			args[7] = "makesym";
		else
			args[7] = "nosym";

		Path out_path = new Path("mvprep_out");
		FileSystem fs = FileSystem.get(conf);
		fs.delete( out_path );

		ToolRunner.run(conf, new MatvecPrep(), args);

		return out_path;
	}

	public static Path MvBlock(Configuration conf, Path edge_block_path, Path vector_block_path, int number_nodes, int block_width, int nreducer) throws Exception{
		System.out.println("Running MvBlock: edge_block_path=" + edge_block_path.getName() + ", block_width=" +block_width);

		String [] args = new String[9];
		args[0] = new String("" + edge_block_path);
		args[1] = new String("" + vector_block_path );
		args[2] = "mvblock_tempmv";
		args[3] = "mvblock_out";
		args[4] = new String("" + number_nodes);
		args[5] = new String("" + nreducer);
		args[6] = new String("" + edge_block_path);
		args[7] = "binary";
		args[8] = new String("" + block_width);


		Path tempmv_path = new Path("mvblock_tempmv");
		Path out_path = new Path("mvblock_out");

		FileSystem fs = FileSystem.get(conf);
		fs.delete(tempmv_path, true);
		fs.delete(out_path, true);

		ToolRunner.run(conf, new MatvecBlock(), args);

		return out_path;
	}

	public static double DotProductBlock(Configuration conf, int nreducer, Path p1, Path p2, int block_width) throws Exception{
		System.out.println("Running DotProductBlock: p1=" + p1.getName() + ", p2=" + p2.getName());
		String [] args = new String[4];
		args[0] = new String("" + nreducer);
		args[1] = new String( p1.getName() );
		args[2] = new String( p2.getName() );
		args[3] = new String("" + block_width);
		ToolRunner.run(conf, new DotProductBlock(), args);
		System.out.println("Done DotProduct: p1=" + p1.getName() + ", p2=" + p2.getName());

		String local_output_path = "lanczos/dp";
		Path dp_output2 = new Path("dp_output2");

		FileSystem lfs = FileSystem.getLocal(conf);
		lfs.delete(new Path("lanczos/dp"), true);

		FileSystem fs = FileSystem.get(conf);
		fs.copyToLocalFile(dp_output2, new Path(local_output_path) ) ;
		double result = PegasusUtils.readLocaldirOneline(local_output_path);

		
		lfs.delete(new Path("lanczos/dp"), true);//FileUtil.fullyDelete( fs.getLocal(conf), new Path(local_output_path));


		return result;
	}

	public static Path SaxpyBlock(Configuration conf, int nreducer, Path py, Path px, double a, int block_width) throws Exception{
		System.out.println("Running SaxypyBlock: py=" + py.getName() + ", px=" + px.getName() + "a="  + a);
		String [] args = new String[5];
		args[0] = new String("" + nreducer);
		args[1] = new String(py.getName());
		args[2] = new String(px.getName() );
		args[3] = new String("" + a);
		args[4] = new String("" + block_width);
		int saxpy_result = ToolRunner.run(conf, new SaxpyBlock(), args);
		System.out.println("Done SaxpyBlock: py=" + py.getName() + ", px=" + px.getName() + "a="  + a);


		FileSystem fs = FileSystem.get(conf);
		fs.delete(py, true);

		if( saxpy_result == 1 )
			fs.rename(new Path("saxpy_output1"), py );
		else
			fs.rename(new Path("saxpy_output"), py );
		
		return py;
	}

	// ||y||_2
	public static double L2normBlock(Configuration conf, int nreducer, Path px, int block_width) throws Exception{
		System.out.println("Running L2norm: px=" + px.getName());

		String [] args = new String[2];
		args[0] = new String("" + px.getName());
		args[1] = new String("" + block_width);

		ToolRunner.run(conf, new L2normBlock(), args);
		double result = LinearAlgebraUtils.read_l2norm_result(conf);
		
		return result;
	}

	public static Path ScalarMultBlock(Configuration conf, Path py, double s, int block_width) throws Exception{
		System.out.println("Running ScalarMultBlock: py=" + py.getName() + ", s=" +s);

		String [] args = new String[3];
		args[0] = new String(py.getName());
		args[1] = new String(""+s );
		args[2] = new String("" + block_width);

		ToolRunner.run(conf, new ScalarMultBlock(), args);

		FileSystem fs = FileSystem.get(conf);
		fs.delete(py, true);

		fs.rename(new Path("smult_output"), py );
		
		return py;
	}
}
