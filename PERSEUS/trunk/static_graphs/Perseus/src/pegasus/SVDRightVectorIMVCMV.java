/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: SVDRightVectorIMVCMV.java
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
public class SVDRightVectorIMVCMV extends Configured implements Tool 
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
		final int result = ToolRunner.run(new Configuration(), new SVDRightVectorIMVCMV(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("SVDRightVectorIMVCMV <edge_path> <k> <v_vector_path> <nreducer>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 4 ) {
			return printUsage();
		}

		int i,j;
		String matrix_path_str = args[0];
		Path edge_path = new Path(matrix_path_str);

		int k = Integer.parseInt(args[1]);
		Path v_vector_path = new Path(args[2]);
		int nreducer = Integer.parseInt(args[3]);

		System.out.println("\nRunning SVDRightVectorIMVCMV... edge_path=" + args[0] + ", k=" + k + ", v_vector_path=" + args[2] );
		final FileSystem fs = FileSystem.get(getConf());

		// Load ritz.map
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( "ritz.map" ), "UTF8"));
		double []eig_val = new double[k];
		String cur_str = in.readLine();
		int m = 0;
		String[] tokens = cur_str.split("\t");
		for(i = 0; i < tokens.length; i++) {
			eig_val[i] = Double.parseDouble( tokens[i] );
			System.out.println("Eigval [ " + i + " ] = " + eig_val[i]);
		}
		in.close();


		for(i = 1; i <= k; i++) {
			String cur_ucol_path_str = "rz_u_svd_" + i;
			String intermed_path_str = "rz_u_scaled";
			LinearAlgebraUtils.ScalarMult(getConf(), new Path(cur_ucol_path_str), new Path(intermed_path_str), 1.0/eig_val[i-1]);

			String cur_vcol_path_str = "rz_v" + i;
			PegasusUtils.MatvecNaiveSS(getConf(), nreducer, matrix_path_str, intermed_path_str, cur_vcol_path_str, 1, 0);
		//	Path r_path = LinearAlgebraUtils.VecarrVecCache( getConf(), "rz_v", k, nreducer, "ritz.map");
		}

		System.out.println("SVDRightVectorIMVCMV done. V vectors are stored in HDFS " + args[2] + "...");

		return 0;
    }
}

