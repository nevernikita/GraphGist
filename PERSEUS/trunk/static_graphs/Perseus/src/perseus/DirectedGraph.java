package perseus;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import pegasus.DegDist;
import pegasus.HEigen;
import pegasus.Hadi;
import pegasus.MakeSymEdge;
import pegasus.PagerankNaive;
import pegasus.QToRitzMatrix;
import pegasus.RitzVectorCMV;
import pegasus.SVDRightVectorIMVCMV;

/**
 * DirectedGraph.java: the class that contains all the additional algorithms
 *                     for mining directed graphs.
 */

/**
 * @author dkoutra
 *
 */
public class DirectedGraph extends PlainGraph implements DirectedGraphInterface {

	/**
	 * @param edgeFile
	 */
	public DirectedGraph( String edgeFile, int reducers, long nodes, String graphName, String enc, String nosymOrmakesym, int evalsNo, boolean debug, int iterations ) {
		super( edgeFile, reducers, nodes, graphName, enc, nosymOrmakesym, evalsNo, debug, iterations);
		computeDirectedStatistics();
	}

	
	public void computeDirectedStatistics(){
		computeInDegree();
		if ( debug ) 
			System.out.println("Finished executing the in-Degree Distribution...");
		computeOutDegree();
		if ( debug ) 
			System.out.println("Finished executing the out-Degree Distribution...");
		computePagerankD();
		if ( debug )
			System.out.println("Finished executing the directed PageRank...");
		computeRadiusD();
		if ( debug )
			System.out.println("Finished executing the directed Radius computation...");
		computeSVD();
		if ( debug )
			System.out.println("Finished executing the SVD computation...");
		
	}


	@Override
	public void computeInDegree() {
		String[] args = new String[]{edgeFile, "dd_node_deg_in", "dd_deg_count_in", "in", Integer.toString(reducers)};
		try {
			DegDist.main(args);
			Perseus.moveFolders(outputFolder, "dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public void computeOutDegree() {
		String[] args = new String[]{edgeFile, "dd_node_deg_out", "dd_deg_count_out", "out", Integer.toString(reducers)};
		try {
			DegDist.main(args);
			Perseus.moveFolders(outputFolder, "dd");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}


	@Override
	public void computePagerankD() {
		String[] pr_args = new String[]{edgeFile, "pr_tempmv_madeSym", "pr_output_madeSym", Long.toString(nodes), Integer.toString(reducers), Integer.toString(iterations), "nosym", "new", "pr_vector_madeSym", "pr_minmax_madeSym", "pr_distr_madeSym" }; 
		try {
			PagerankNaive.main(pr_args);
			Perseus.moveFolders(outputFolder, "pr");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}


	@Override
	public void computeRadiusD() {
		String[] hadi_args = new String[]{edgeFile, outputFolder+"/hadi_curbm", outputFolder+"/hadi_tempbm", outputFolder+"/hadi_nextbm", outputFolder+"/hadi_output", Long.toString(nodes), Integer.toString(32), Integer.toString(reducers), enc, "newbm", "makesym", "max", outputFolder+"/hadi_output_madeSym", outputFolder+"/hadi_radius_summary_madeSym" };
		try {
			Hadi.main(hadi_args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void computePCA() {
		System.out.println("Calling PCA from the Directed Graph instead of the Plain... Success!");
		String[] makesym_args = new String[]{edgeFile, edgeFile+"_sym", Integer.toString(reducers) };
		String[] heigen_args = new String[]{edgeFile+"_sym", "lz_q1", Long.toString(nodes), Integer.toString(reducers), "nosym", Integer.toString(iterations), Integer.toString(1), "LM", Integer.toString(evalsNo), Integer.toString(1), "eig" };
		try {
			MakeSymEdge.main(makesym_args);
			HEigen.main(heigen_args);
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("lanczos.ab"))));
			String strLine;
			String[] parts = {};
			while ( (strLine = br.readLine()) != null )
				parts = strLine.split("\t");
			String[] qtoritz_args = new String[]{parts[0], Integer.toString(reducers), "qmatrix", "null" };
			QToRitzMatrix.main(qtoritz_args);
			String[] ritzvec_args = new String[]{parts[0], Integer.toString(evalsNo), "lanczos.ab", Integer.toString(reducers), "LM", "qmatrix", "eig" };
			RitzVectorCMV.main(ritzvec_args);
			Perseus.moveFolders(outputFolder, "rz_");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void computeSVD() {
		String[] heigen_args = new String[]{edgeFile, "lz_q1", Long.toString(nodes), Integer.toString(reducers), "nosym", Integer.toString(iterations), Integer.toString(1), "LM", Integer.toString(evalsNo), Integer.toString(1), "svd" };
//		String[] svdrightvec_args = new String[]{edgeFile, Integer.toString(evalsNo), "rz_v", Integer.toString(reducers)};
		try {
			HEigen.main(heigen_args);
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("lanczos.ab"))));
			String strLine;
			String[] parts = {};
			while ( (strLine = br.readLine()) != null )
				parts = strLine.split("\t");
			String[] qtoritz_args = new String[]{parts[0], Integer.toString(reducers), "qmatrix", "null" };
			QToRitzMatrix.main(qtoritz_args);
			String[] ritzvec_args = new String[]{parts[0], Integer.toString(evalsNo), "lanczos.ab", Integer.toString(reducers), "LM", "qmatrix", "svd" };
			RitzVectorCMV.main(ritzvec_args);
			String[] svdrightvec_args = new String[]{edgeFile, Integer.toString(evalsNo), "rz_v", Integer.toString(reducers)};
			SVDRightVectorIMVCMV.main(svdrightvec_args);
			Perseus.moveFolders(outputFolder, "rz_");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
