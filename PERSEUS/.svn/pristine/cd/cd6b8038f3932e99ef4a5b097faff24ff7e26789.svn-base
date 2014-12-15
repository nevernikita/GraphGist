/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: RitzVectorCMM.java
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
public class RitzVectorCMM extends Configured implements Tool 
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
		final int result = ToolRunner.run(new Configuration(), new RitzVectorCMM(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("RitzVectorCMM <in_path>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (String[] args) throws Exception
    {
		if( args.length != 6 ) {
			return printUsage();
		}

		int i,j;
		int m = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		String td_filename = args[2];
		int nreducer = Integer.parseInt(args[3]);
		String eig_mode = args[4];
		Path edge_path = new Path(args[5]);

		System.out.println("\nRunning RitzVectorCMM... m=" + m + ", k=" + k + ",td_filename=" + td_filename  + ", eig_mode=" + eig_mode);
		final FileSystem fs = FileSystem.get(getConf());


		// Load Tm matrix
		alpha_beta ab = LinearAlgebraUtils.load_alpha_beta(m+1);
		Matrix Tm = LinearAlgebraUtils.getT(m, ab.alpha, ab.beta);

        EigenvalueDecomposition VD = new EigenvalueDecomposition(Tm);
		Matrix V = VD.getV();
		Matrix D = VD.getD();
		double[] v_arr = VD.getRealEigenvalues();
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

		FileWriter ritz_info_file = new FileWriter("ritz.map");
		BufferedWriter fout = new BufferedWriter (ritz_info_file);

		LinearAlgebraUtils.save_matrix_to_disk( V, m, k, "lanczos.Qk" );

		System.out.println("\n\t*** Computing All(" + m + ") Ritz vector...");

		String m_str = "";
		for(i = 1; i <= m; i++)
			m_str += "" + i;

		args = new String[5];
		args[0] = new String( "" + edge_path );
		args[1] = new String("" + nreducer);
		args[2] = new String("lanczos.Qk");
		args[3] = new String("" + m);
		args[4] = new String("" + k);
		ToolRunner.run(getConf(), new MatmatCache(), args);

		fout.close();
		System.out.println("RitzVectorCMM done. Ritz vectors are saved in rz_vN. Eigval info is saved in ritz.map");

		return 0;
    }
}

