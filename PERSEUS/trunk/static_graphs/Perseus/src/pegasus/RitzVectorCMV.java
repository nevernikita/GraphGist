/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: RitzVectorCMV.java
 - Lanczos
Version: 0.9
Author Email: U Kang(ukang@cs.cmu.edu), Christos Faloutsos(christos@cs.cmu.edu)
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

// y = y + ax
public class RitzVectorCMV extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////
	protected int nreducers = 1;
	protected double alpha[];
	protected double beta[];

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new RitzVectorCMV(), args);

		return;
//		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("RitzVectorCMV <in_path>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 7 ) {
			return printUsage();
		}
		
		int i,j;
		int is_svd = 0;	// svd or eig
		int m = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		String td_filename = args[2];
		int nreducer = Integer.parseInt(args[3]);
		String eig_mode = args[4];
		Path edge_path = new Path(args[5]);
		if( args[6].startsWith("svd") )
			is_svd = 1;

		System.out.println("\nRunning RitzVectorCMV for " + args[6] + "... m=" + m + ", k=" + k + ",td_filename=" + td_filename  + ", eig_mode=" + eig_mode);
		final FileSystem fs = FileSystem.get(getConf());


		// Load Tm matrix
		alpha_beta ab = LinearAlgebraUtils.load_alpha_beta(m+1);
		Matrix Tm = LinearAlgebraUtils.getT(m, ab.alpha, ab.beta);

        EigenvalueDecomposition VD = new EigenvalueDecomposition(Tm);
		Matrix V = VD.getV();
		Matrix D = VD.getD();
		double[] v_arr = VD.getRealEigenvalues();
		for(i = 0; i < v_arr.length; i++) {
				System.out.println("v_arr[" + i + "] = " + v_arr[i]  );
		}

		if (is_svd == 1) {
			for(i=0; i < m; i++) {
				if( v_arr[i] < 0 )
					v_arr[i] = 0;
				else
					v_arr[i] = Math.sqrt(v_arr[i]);

				double D_i_i = D.get(i, i);
				if( D_i_i < 0 )
					D.set( i, i, 0 );
				else
					D.set( i, i, Math.sqrt(D_i_i) );

				System.out.println("v_arr[" + i + "] = " + v_arr[i] + ", D(i,i) = " + D.get(i,i) );
			}
		}

		if( eig_mode.equals("LA") )
			Arrays.sort(v_arr);	// arrays are sorted in the ascending order
		else if( eig_mode.equals("LM") ) {
			Double[] v_arr_obj = new Double[m];
			for(i=0; i < m; i++)
				v_arr_obj[i] = v_arr[i];
			Arrays.sort( v_arr_obj, LinearAlgebraUtils.getAbsDoubleAscComparator() );
			for(i=0; i < m; i++)
				v_arr[i] = v_arr_obj[i];
		}

		FileWriter ritz_info_file = new FileWriter("ritz_" + args[6] + ".map");
		BufferedWriter fout = new BufferedWriter (ritz_info_file);

		for(i = 1; i <= k; i++) {
			System.out.println("\n\t*** Computing " + i  + "/" + k + "th Ritz vector...");
			double cur_eig = v_arr[m-i];
			System.out.println("Searching ... cur_eig=" + cur_eig + ", j=" + (m-1) + "..0");
			for(j = m-1; j>=0; j--) {
				if( D.get(j,j) == cur_eig )
					break;
			}
			//fout.write("" + i + "\t" + cur_eig + "\n");
			if( i != 1 )
				fout.write("\t");
			fout.write("" + cur_eig);

			String local_filename = "ritz.v" + i;
			LinearAlgebraUtils.save_vector_to_disk( V, m, j, local_filename);

//			Path rv_output = new Path("rz_u" + i);
			Path rv_output = new Path("rz_u_" + args[6] + "_" + i);
			Path r_path = LinearAlgebraUtils.MatvecCache( getConf(), m, edge_path, nreducer, local_filename);

			fs.delete(rv_output);
			fs.rename(r_path, rv_output);
		}

		fout.close();
		//System.out.println("Eigenvectors are saved in HDFS rz_v1 to rz_v" + k +". Eigenvalues are saved in ritz.map");
		System.out.println("RitzVectorCMV done. Eigenvectors are saved in HDFS rz_u_" +  args[6] + "_1 to rz_u_" +  args[6] + "_" + k +". Eigenvalues are saved in ritz.map");

		return 0;
    }
}

