/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Copyright (c) 2009
    U Kang and Christos Faloutsos
    All Rights Reserved

You may use this code without fee, for educational and research purposes.
Any for-profit use requires written consent of the copyright holders.

-------------------------------------------------------------------------
File: RitzVectorIMV.java
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
public class RitzVectorIMV extends Configured implements Tool 
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
		final int result = ToolRunner.run(new Configuration(), new RitzVectorIMV(), args);

		System.exit(result);
    }


    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("RitzVectorIMV <in_path>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
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

		Path t_ev_path = new Path("t_ev_path");

		System.out.println("\nRunning RitzVectorIMV... m=" + m + ", k=" + k + ",td_filename=" + td_filename  + ", eig_mode=" + eig_mode);
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

		for(i = 1; i <= k; i++) {
			System.out.println("\n\t*** Computing " + i  + "/" + k + "th Ritz vector...");
			double cur_eig = v_arr[m-i];
			for(j = m-1; j>=0; j--) {
				if( D.get(j,j) == cur_eig )
					break;
			}
			fout.write("" + i + "\t" + cur_eig + "\n");

			String local_filename = "ritz.v" + i;
			LinearAlgebraUtils.save_vector_to_disk_2col( V, m, j, local_filename);

			fs.delete(t_ev_path);
			fs.copyFromLocalFile( true, new Path("./" + local_filename), t_ev_path );
			Path rv_output = new Path("rz_v" + i);

			//Path out_path = LinearAlgebraUtils.NaiveMVSecondarySort( getConf(), nreducer, edge_path, t_ev_path);
			MatvecNaiveSecondarySort.MatvecNaiveSS( getConf(), nreducer, "" + edge_path, "" + t_ev_path, "mvnaive_out", 0, 0);
			Path out_path = new Path("mvnaive_out");

			fs.delete(rv_output);
			fs.rename(out_path, rv_output);
		}

		fout.close();
		System.out.println("RitzVectorIMV done. Ritz vectors are saved in rz_vN. Eigval info is saved in ritz.map");

		return 0;
    }
}

