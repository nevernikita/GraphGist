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
File: HEigen.java
 - Compute top k eigenvalues and eigenvectors of a matrix.
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

// HEigen Main Class
public class HEigen extends Configured implements Tool 
{
    public static int MAX_ITERATIONS = 64;

    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
    protected Path edge_path = null;
    protected Path initb_path = null;
	protected Path q_path[];
	String edge_path_str;
	String q_path_str[];
	protected int number_nodes = 0;
	protected int nreducer = 1;
	protected int makesym = 0;
	protected int n;
	protected double alpha[];
	protected double beta[];
	protected String eig_mode = "LM";
	protected int eig_k = 6;					// number of eigenvalues/eigenvectors we want.
	protected double [] eigval_prev = null;
	protected double [] eigval_cur = null;
	protected boolean isEigvalPrevSet = false;
	protected boolean isEigConverged = false;
	boolean bSelectiveOrth = false;
	FileSystem fs = null;

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new HEigen(), args);

		return;
//		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("HEigen <edge_path> <init_b_path> <# of nodes> <# of reducers> <makesym or nosym> <max_iteration> <iteration_start_index> <eig_mode> <k> <1 or 0: sel. ortho> <svd or eig>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// save the tri-diagonal matrix to local file 'lanczos.ab'
	protected void save_tridiagonal_matrix( double []alpha, double []beta, int max_iteration ) throws IOException
	{
		int i;
		String file_name = "lanczos.ab";
		FileWriter file = new FileWriter(file_name);
		BufferedWriter out = new BufferedWriter (file);

		
		System.out.println("max_iteration = " + max_iteration);
		System.out.println("Trying to write the lanczos.ab file...");

		
		for(i=1; i <= max_iteration; i++)
		{
			out.write("" + i + "\t" + alpha[i] + "\t" + beta[i] + "\n");
		}
		out.close();
		System.out.println("The alphas and betas are saved in " + file_name);


		file_name = "lanczos.trimat";
		file = new FileWriter(file_name);
		out = new BufferedWriter (file);

		double [][]T = new double[max_iteration][max_iteration];
		int j;
		for(i=0; i<max_iteration; i++) {
			for(j=0; j<max_iteration; j++)
				T[i][j] = 0;

			T[i][i] = alpha[i+1];
			if( (i-1) >= 0 )
				T[i][i-1] = beta[i];
			if( (i+1) < max_iteration)
				T[i][i+1] = beta[i+1];
		}

		for(i=0; i < max_iteration; i++) {
			for(j=0; j<max_iteration; j++) {
				out.write("" + T[i][j]);
				if( j < max_iteration - 1 )
					out.write("\t");
				else
					out.write("\n");
			}
		}
		out.write("\n");
		out.close();
		System.out.println("The tridiagonal matrix T is saved in " + file_name);
	}

	// compare the two eigenvector arrays.
	// return value : true (same)
	//				  false: not same
	boolean isSameEigval(double[] eig1, double[] eig2, int k) {
		final double threshold = 1e-4;
		int diff_count = 0;

		for(int i = 0; i < k; i++) {
			if( Math.abs(eig1[i] - eig2[i]) > threshold )
				diff_count++;
		}

		System.out.println("Eigenvalues " + (k-diff_count) + " of " + k + " converged.");

		if( diff_count > 0 )
			return false;

		return true;
	}

	// return desired k eigenvalues
	double [] getEigvalOfInterest( Double[] eig, int n, int k) {
		double[] ret_eig = new double[k];

		if( eig_mode.equals("LA") ) {
			Arrays.sort( eig, LinearAlgebraUtils.getDoubleDscComparator() );
		} else if(eig_mode.equals("LM")) {
			Arrays.sort( eig, LinearAlgebraUtils.getAbsDoubleDscComparator() );
		}

		if( k > n )
			k = n;

		double[] prim_eig = new double[k];
		for(int i = 0; i < k; i++)
			prim_eig[i] = eig[i];

		ret_eig = Arrays.copyOf(prim_eig, eig_k) ;
		Arrays.sort(ret_eig);

		return ret_eig;
	}

	// compute eigenvalues
	boolean compute_eigval(int nrow) {
		int i;

		Matrix Tm = LinearAlgebraUtils.getT(nrow, alpha, beta);

		// get eigenvalues of Tm
        EigenvalueDecomposition VD = new EigenvalueDecomposition(Tm);
		double[] v_arr = VD.getRealEigenvalues();
		Arrays.sort(v_arr);	// arrays are sorted in the ascending order

		System.out.print("All Eigenvalues of Tm(" + nrow + " by " + nrow + "): ");
		for(i=0; i < nrow - 1; i++)
			System.out.print("" + v_arr[i] + "\t");
		System.out.println("" + v_arr[i]);

		Double[] v_arr_obj = new Double[nrow];
		for(i=0; i < nrow; i++)
			v_arr_obj[i] = v_arr[i];

		// update eigval_prev, eigval_cur, isEigvalPrevSet
		if( isEigvalPrevSet ) {
			eigval_prev = Arrays.copyOf(eigval_cur, eig_k);

			eigval_cur = getEigvalOfInterest( v_arr_obj, nrow, eig_k );

			if( isSameEigval(eigval_prev, eigval_cur, eig_k) )
				isEigConverged = true;
			else
				isEigConverged = false;
		} else {
			isEigConverged = false;

			if( nrow >= eig_k ) {
				isEigvalPrevSet = true;

				eigval_cur = getEigvalOfInterest( v_arr_obj, nrow, eig_k );
			}
		}

		// print top k eigenvalues of interests
		if( nrow >= eig_k ) {
			System.out.print("\n\nTOPK_Eigenvalues(mode=" + eig_mode +")\tITER["+ nrow + "]\t" );
			for(i=0; i < eig_k - 1; i++)
				System.out.print("" + eigval_cur[i] + "\t");
			System.out.println("" + eigval_cur[i] + "\n");
		}

		return isEigConverged;
	}

	// check whether eigenvalues converged
	boolean isEigenvalueConverged() {
		return isEigConverged;
	}

	// perform selective orthogonalization
	int selective_orthogonalize(int i, double beta_i, Path v_path) throws Exception{
		double eps = 2.204e-016;	// machine epsilon
		int reortho_count = 0;
		Matrix Tm = LinearAlgebraUtils.getT(i, alpha, beta);

		// get eigenvalues of Tm
        EigenvalueDecomposition VD = new EigenvalueDecomposition(Tm);
		Matrix V = VD.getV();
		double[] v_arr = VD.getRealEigenvalues();
		Arrays.sort(v_arr);	// arrays are sorted in the ascending order

		double error_bound = Math.abs(Math.sqrt(eps) * v_arr[i-1]);
		int j;
		System.out.println("Error Bound: " + error_bound);
		for(j = 1; j <= i; j++) {
			double v_ij = V.get(i-1,j-1);
			double cur_error = Math.abs(beta_i * v_ij);
			System.out.println("Error of " + j + "/" + i + " th vector: " + cur_error);
			if( cur_error <= error_bound ) {
				System.out.println("Too small error! v need to be reorthogalized by " + j + "th Ritz vector");
				reortho_count++;

				System.out.println("Reorthogonalizing against " + j + " th Ritz vector");
				// save q_j to disk
				LinearAlgebraUtils.save_vector_to_disk( V, i, j-1, "lanczos.so.qj");
				Path r_path = LinearAlgebraUtils.VecarrVecCache( getConf(), "lz_q", i, nreducer, "lanczos.so.qj");

				// compute (r^T v)
				double rtv = LinearAlgebraUtils.DotProduct(getConf(), nreducer, r_path.getName(), v_path.getName());

				LinearAlgebraUtils.Saxpy(getConf(), nreducer, v_path, r_path, -1 * rtv);
			}
		}

		return reortho_count;
	}

	// submit the map/reduce job.
    public int run (String[] args) throws Exception
    {
		int final_iteration = 1;;
		int max_iteration = MAX_ITERATIONS;
		int is_svd = 0;

		if( args.length != 11 ) {
			return printUsage();
		}

		edge_path_str = args[0];
		edge_path = new Path(edge_path_str);
		initb_path = new Path(args[1]);
		number_nodes = Integer.parseInt(args[2]);
		nreducer = Integer.parseInt(args[3]);
		if(args[4].startsWith("makesym"))
			makesym = 1;
		max_iteration = Integer.parseInt(args[5]);
		int iteration_start_index = Integer.parseInt(args[6]);
		eig_mode = args[7];
		eig_k = Integer.parseInt(args[8]);
		if( args[9].equals("1") )
			bSelectiveOrth = true;
		if( args[10].startsWith("svd") )
			is_svd = 1;

		eigval_prev = new double[eig_k];
		eigval_cur = new double[eig_k];

		System.out.println("Starting HEigen for " + args[10] + ": " + max_iteration + " iterations.");
		System.out.println("edge_path=" + args[0]+ ", number_nodes=" + number_nodes + ", nreducer=" + nreducer + ", makesym=" + makesym);
		fs = FileSystem.get(getConf());
		System.out.println("selective orthogonalization: " + bSelectiveOrth);

		FileSystem lfs = FileSystem.getLocal(getConf());
		lfs.delete(new Path("lanczos/"), true);

		alpha = new double[max_iteration+1];
		beta = new double[max_iteration+1];
		beta[0] = 0;

		// make q_path[]. q_1 is saved in q_path[1], and so on.
		q_path = new Path[max_iteration+2];
		q_path_str = new String[max_iteration+2];
		for(n = 1; n <= max_iteration+1; n++) {
			q_path_str[n] = "lz_q" + n;
			q_path[n] = new Path(q_path_str[n]);
			if( n > iteration_start_index )
				fs.delete(q_path[n], true);
		}

		// Load alpha and beta from file if possible.
		if( iteration_start_index > 1 ) {
			alpha_beta ab = LinearAlgebraUtils.load_alpha_beta(max_iteration+1);
			alpha = ab.alpha;
			beta = ab.beta;
		} else {
			String q1_path_name = "lz_q1";
			// run init vector and normalize
			args = new String[3];
			args[0] = new String( "initb_path" );
			args[1] = new String("" + number_nodes);
			args[2] = new String("" + nreducer);
			ToolRunner.run(getConf(), new InitB(), args);

			// normalize
			args = new String[1];
			args[0] = "initb_path";

			ToolRunner.run(getConf(), new L2norm(), args);
			double scalar = LinearAlgebraUtils.read_l2norm_result(getConf());
			lfs.delete(new Path("lanczos/l2norm"), true);

			// multiply by scalar
			args = new String[2];
			args[0] = "initb_path";
			args[1] = new String("" + 1/scalar);
			ToolRunner.run(getConf(), new ScalarMult(), args);
			fs.delete(new Path(q1_path_name), true);
			fs.rename(new Path("smult_output"), new Path(q1_path_name) );
		}

		// Run HEigen iterations
		for (n = iteration_start_index; n <= max_iteration; n++) {
			//v = Aq_n
			System.out.println("\n\t*** HEigen iteration " + n  + "/" + max_iteration);
			System.out.println("Computing v=Aq" + n + "...");

			String aqn_output_path_str = "aqn_output";
			Path aqn_output_path = new Path(aqn_output_path_str); //= mv( edge_path, q_path[n] );

			if( is_svd == 0 )
				PegasusUtils.MatvecNaiveSS( getConf(), nreducer, edge_path_str, q_path_str[n], aqn_output_path_str, 0, 0 );
			else { // A A^T v 
				String at_v_path_str = "at_v_output";
				// A^T v
				PegasusUtils.MatvecNaiveSS( getConf(), nreducer, edge_path_str, q_path_str[n], at_v_path_str, 1, 0 );
				// A (A^T v)
				PegasusUtils.MatvecNaiveSS( getConf(), nreducer, edge_path_str, at_v_path_str, aqn_output_path_str, 0, 0 );
			}

			System.out.println("\nResult of v = Aq" + n + " is saved in HDFS " + aqn_output_path_str );

			//alpha_n = (q_n)^T v
			System.out.println("\nComputing alpha[" + n + "]...");
			alpha[n] = LinearAlgebraUtils.DotProduct(getConf(), nreducer, q_path[n].getName(), aqn_output_path.getName());
			System.out.println("Result of alpha[" + n + "] = (q" + n + ")^T v is saved in HDFS dp_output2.");
			System.out.println("alpha[" + n + "]= " + alpha[n]);

			// (5) v = v - beta_{n-1}q_{n-1} - (alpha_n)q_n

			// (5.1) v = v - beta_{n-1}q_{n-1}
			Path v_path = aqn_output_path;
			if( n != 1 ) {
				v_path = LinearAlgebraUtils.Saxpy( getConf(), nreducer, aqn_output_path, q_path[n-1], -1*beta[n-1]);
			}

			// (5.2) v = v - (alpha_n)q_n
			Path saxpy_output_path = LinearAlgebraUtils.Saxpy( getConf(), nreducer, v_path, q_path[n], -1*alpha[n]);

			v_path = new Path("lz_vpath");
			fs.delete(v_path, true);
			fs.rename(saxpy_output_path, v_path);


			// (6) beta_n = ||v||
			System.out.println("\nComputing beta[" + n + "]...");
			beta[n] = LinearAlgebraUtils.L2norm(getConf(), nreducer, v_path);
			System.out.println("beta[" + n + "] = " + beta[n] );

			int num_orthogonalized = 0;
			if(bSelectiveOrth) {
				num_orthogonalized = selective_orthogonalize(n, beta[n], v_path);
				System.out.println("Number of orthongalization: " + num_orthogonalized);

				if( num_orthogonalized > 0 ) {
					// recalculate beta[n]
					System.out.println("Recomputing beta[" + n + "]");
					beta[n] = LinearAlgebraUtils.L2norm(getConf(), nreducer, v_path);
					System.out.println("beta[" + n + "] = " + beta[n] );
				}

				if( num_orthogonalized > n-1 ) {
					System.out.println("The new vector converged. finishing...");
					save_tridiagonal_matrix( alpha, beta, n );
					break;
				}
			}

			if( beta[n] == 0 ) {
				System.out.println("beta[" + n + "] = 0. finishing...");
				System.out.println("Saving the tridiagonal matrix");
				save_tridiagonal_matrix( alpha, beta, n );
				break;
			}

			// (10) q_{n+1} = v / beta_n
			System.out.println("Computing q" + (n+1) + "...");
			Path qnp1_path_temp = LinearAlgebraUtils.ScalarMult(getConf(), v_path, 1/beta[n]);
			System.out.println("renaming " + qnp1_path_temp.getName() + " to " + q_path[n+1].getName() );
			fs.rename(qnp1_path_temp, q_path[n+1]);
			System.out.println("q_{" + (n+1) + "} is calculated.");


			// Compute Eigenvalues of Tm
			compute_eigval(n);
			if( isEigenvalueConverged() ) {
				save_tridiagonal_matrix( alpha, beta, n );
				System.out.println("Eigenvalue converged. exiting...");
				break;
			}

			System.out.println("Saving the tridiagonal matrix");
			save_tridiagonal_matrix( alpha, beta, n );
		}

		final_iteration = n;

		if( n > max_iteration )
			System.out.println("\nReached the max iterations. finishing...");
		System.out.println("Summarizing alpha[] and beta[]...");
		System.out.println("n\talpha\t\tbeta");
		for(int i = 1; i <= max_iteration; i++) {
			System.out.println("" + i + "\t" + alpha[i] + "\t\t" + beta[i] );
		}

		return final_iteration;
    }
}

