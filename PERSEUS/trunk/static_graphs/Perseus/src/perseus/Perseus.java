package perseus;

import java.io.File;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


/*
 * TODO: weighted degree in Pegasus -> Check!
 *       
 * 
 * To run: -g Polly_weighted_fulfilled.edge -q rwr_query -w -debug
 *  OR     -g catepillar_star_symm.edge -q rwr_query -debug
 * 
 * What to do?
 *  - with RWR? what kind of plots do we want to show?
 * 
 * Known Issues:
 *  - if the graph is weighted with Double weights, the block connected components algorithm will lead to 
 *    numberFormatException - check line 83 in MatvecPrep.java to see why this leads to a problem. The input
 *    graph is expected to be of the type < srcID \t dstID >
 *  - similarly, the hadi block is not working properly for double weights
 *  - Computation of triangles for plain graph throws some exception: Input path does not exist: file:/Users/dkoutra/Dropbox/GraphGist_v2/rz_u1
 *  - Block computations for connected components, radius etc. ignored.
 */


public class Perseus {



	public enum Info { MAX, ORIGINAL, SYMMETRIC };


	/**
	 * prefix: String denoting the prefix of the folders to be moved in 'folder' 
	 *  
	 */
	public static void moveFolders( String folder, String prefix ){

		File destDir = null; 

		File dir = new File(".");
		if(!dir.isDirectory()) throw new IllegalStateException("This is not a directory...");
		for(File file : dir.listFiles()) {
			if(file.getName().startsWith(prefix)){
				destDir = new File( folder + "/" + file);
				file.renameTo(destDir);
			}
		}
		System.out.println("Moved folders starting with " + prefix + ".");
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		/* TO DO: some parsing of the graph                           */
		/* 3. renumber the nodes in the graph if not sequential?      */

		/* read the command line arguments */
		CmdSettings cmdSettings = new CmdSettings();
	    JCommander cmd = new JCommander(cmdSettings);
//		JCommander cmd = new JCommander(cmdSettings, args);
		boolean debug = false;
		try {
	    cmd.parse(args);
		debug = cmdSettings.getDebug();
		} catch (ParameterException ex) {
		        System.out.println(ex.getMessage());
		        cmd.usage();
		        System.exit(1);
		 }
		

		System.out.println("****************************************");
		System.out.println("************* P E R S E U S ************");
		System.out.println("****************************************");
		System.out.println("* Input: " + cmdSettings.getGraphName());
		System.out.println("\t with nodes: " + cmdSettings.getNodes());
		System.out.println("* Outputs: 'outputs_" + cmdSettings.getGraphName() + "/'.");
		System.out.println("****************************************");

		/* New graph: count the number of nodes, and find if it is symmetric or not */
		Graph gr = new Graph( cmdSettings.getGraphEdgeFile(), cmdSettings.getReducers(), cmdSettings );

		
		if ( !cmdSettings.isOnlyPlots() ){

			if (debug){
				System.out.println("****************************************");
				System.out.println("* Input: " + cmdSettings.getGraphName());
				System.out.println("\t with nodes: " + cmdSettings.getNodes() + " " + cmdSettings.getSymmetric() );
				System.out.println("* Outputs: " + cmdSettings.getGraphName() + "/'.");
				System.out.println("****************************************");
			}

			SystemCall sc0 = new SystemCall("mkdir " + cmdSettings.getGraphName());
			sc0.execute();
			
			
		/* Execute the algorithms that are appropriate for each class of graph */
		if ( cmdSettings.getSymmetric().equalsIgnoreCase("nosym") && cmdSettings.getWeightedGraph() == false ) {
			PlainGraph g = new PlainGraph( cmdSettings.getGraphEdgeFile(), cmdSettings.getReducers(), cmdSettings.getNodes(), cmdSettings.getGraphName(), cmdSettings.getEncode(), cmdSettings.getSymmetric(), cmdSettings.getEvals(), cmdSettings.getDebug(), cmdSettings.getIterNo() );
		}
		else if ( cmdSettings.getSymmetric().equalsIgnoreCase("nosym") ) {
			WeightedGraph g = new WeightedGraph( cmdSettings.getGraphEdgeFile(), cmdSettings.getReducers(), cmdSettings.getNodes(), cmdSettings.getGraphName(), cmdSettings.getEncode(), cmdSettings.getSymmetric(), cmdSettings.getEvals(), cmdSettings.getDebug(), cmdSettings.getIterNo()  );
		}
		else if ( cmdSettings.getWeightedGraph() == false ) { 
			DirectedGraph g = new DirectedGraph( cmdSettings.getGraphEdgeFile(), cmdSettings.getReducers(), cmdSettings.getNodes(), cmdSettings.getGraphName(), cmdSettings.getEncode(), cmdSettings.getSymmetric(), cmdSettings.getEvals(), cmdSettings.getDebug(), cmdSettings.getIterNo()  );
		}
		else {
			WeightedDirectedGraph g = new WeightedDirectedGraph( cmdSettings.getGraphEdgeFile(), cmdSettings.getReducers(), cmdSettings.getNodes(), cmdSettings.getGraphName(), cmdSettings.getEncode(), cmdSettings.getSymmetric(), cmdSettings.getEvals(), cmdSettings.getDebug(), cmdSettings.getIterNo()    );
		}
	  }
			
		
			
		/* TODO: populate the database within each class of graphs - so that the appropriate types of info are loaded :D */	

		/* Populate the database with the graph statistics */		
//		SystemCall la = new SystemCall("bash create_populateDB.bash " + cmdSettings.getGraphName() );
//		la.execute();
		
		
		
		/* ------------------------------------------------------------------ DISTRIBUTION PLOTS --------------------------------------------------------------------------------- */
		
		
		String[] nameParts = cmdSettings.getGraphName().split("_");
		String graphName_nodash = "";
		for ( int i = 0; i < nameParts.length; i++ )
			graphName_nodash += nameParts[i] + "-";
		
		CreatePlot plot = null;
		
		/* For DIRECTED graphs */
		System.out.println(">>> Symm graph = " + cmdSettings.getSymmetric());
		if ( cmdSettings.getSymmetric().equalsIgnoreCase("makesym")  ) {
			
			/* Degree Distribution Plots */
			plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_indeg_local/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
			plot.callGnuplot();
			plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_outdeg_local/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "out-degree", "count", "distribution", "distr_out-degree");
			plot.callGnuplot();
			
			
			System.out.println(">>> Weighted graph = " + cmdSettings.getWeightedGraph());
			/* Degree Distribution Plots for WEIGHTED graphs */
			if ( cmdSettings.getWeightedGraph() ){
				plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_indeg_weighted_local/part-00000", "=" + graphName_nodash + "==weighted-in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree_weighted");
				plot.callGnuplot();
				plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_outdeg_weighted_local/part-00000", "=" + graphName_nodash + "==weighted-in-degree-distribution=", "out-degree", "count", "distribution", "distr_out-degree_weighted");
				plot.callGnuplot();
			}
			
			/* Pagerank Distribution Plots */
			plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
			plot.callGnuplot();
			plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
			plot.callGnuplot();
			
			/* Radius Distribution Plots */
			plot = new CreatePlot( cmdSettings.getGraphName() + "/hadi_radius_summary_orig_local/part-00000", "=" + graphName_nodash + "==radius-distribution=(directed)=", "radius", "count", "distribution", "distr_radius_directed");
			plot.callGnuplot();
			
//			plot = new CreatePlot( cmdSettings.getGraphName() + "/hadi_radius_summary_orig_local/part-00000", "=" + graphName_nodash + "==radius-block-distribution=(directed)=", "radius", "count", "distribution", "distr_radiusblk_directed");
//			plot.callGnuplot();
			
			
		}
		
		/* === For ALL graphs (undirected / or converted to undirected) === */
		
		/* Degree Distribution Plots */
		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inout_local/part-00000", "=" + graphName_nodash + "==degree-distribution=(undirected)=", "degree", "count", "distribution", "distr_inout-degree");
		plot.callGnuplot();
		
		/* Degree Distribution Plots for WEIGHTED graphs */
		System.out.println(">>> Weighted graph = " + cmdSettings.getWeightedGraph());
		if ( cmdSettings.getWeightedGraph() ){
			plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inout_weighted_local/part-00000", "=" + graphName_nodash + "==degree-distribution=(undirected)=", "degree", "count", "distribution", "distr_inout-degree_weighted");
			plot.callGnuplot();
		}
		
		/* Radius Distribution Plots */
		plot = new CreatePlot( cmdSettings.getGraphName() + "/hadi_radius_summary_orig_local/part-00000", "=" + graphName_nodash + "==radius-distribution=(undirected)=", "radius", "count", "distribution", "distr_radius_undirected");
		plot.callGnuplot();
		
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/hadiblk_radius_summary_orig_local/part-00000", "=" + graphName_nodash + "==radius-block-distribution=(undirected)=", "radius", "count", "distribution", "distr_radiusblk_undirected");
//		plot.callGnuplot();
		
		/* Connected Components Distribution Plots */
		plot = new CreatePlot( cmdSettings.getGraphName() + "/concmpt_summaryout_local/part-00000", "=" + graphName_nodash + "==cc-distribution=(undirected)=", "con_components", "count", "distribution", "distr_cc_undirected");
		plot.callGnuplot();
		
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/concmpt_block_summaryout_local/part-00000", "=" + graphName_nodash + "==cc-block-distribution=(undirected)=", "con_components", "count", "distribution", "distr_ccblk_undirected");
//		plot.callGnuplot();
		
		/* Distribution Plots: move output folders to the input graph directory */
		moveFolders( cmdSettings.getGraphName(), "distr_" );
		
		/**
		 * TODO: plot RWR (Danai: check my notes on what exactly we want to plot) 
		 * 
		 */
		
		/* ------------------------------------------------------------------ 2D DISTRIBUTION PLOTS --------------------------------------------------------------------------------- */
		
		/**
		 * TODO: plot pairwise distributions of statistics (Danai: check my notes)
		 *       Use the create_2d_distribution_plots.bash script for plotting the appropriate statistics.
		 */
		
//		
//		/* what plot do we want for RWR? */
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
//		plot.callGnuplot();
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
//		plot.callGnuplot();
//		
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
//		plot.callGnuplot();
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
//		plot.callGnuplot();
//		
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
//		plot.callGnuplot();
//		plot = new CreatePlot( cmdSettings.getGraphName() + "/dd_deg_count_inDeg/part-00000", "=" + graphName_nodash + "==in-degree-distribution=", "in-degree", "count", "distribution", "distr_in-degree");
//		plot.callGnuplot();

	}


}









/* Algorithm 0: Retrieve info about the graph. Find out if it is symmetric + how many nodes it has. */
//String[] symmAndNodesNo_args = new String[]{ cmdSettings.getGraphEdgeFile(), "out_graphInfo", Integer.toString(cmdSettings.getReducers()) };
//SymmetricAndNumberOfNodes.main(symmAndNodesNo_args);
//
//System.out.println("returning to main program");
//
///* read output file with info about the symmetry of the adjacency matrix + the number of nodes */
//try{
//	FileInputStream fs = new FileInputStream("out_graphInfo/part-00000");
//	DataInputStream ds = new DataInputStream(fs);
//	BufferedReader br = new BufferedReader(new InputStreamReader(ds));
//	String strLine;
//	while (( strLine = br.readLine()) != null)   {
//		String[] parts = strLine.split("\t");
//
//		Info info = Info.valueOf( parts[0].toUpperCase() );
//		switch ( info  ){
//		case MAX: cmdSettings.setNodes( Long.parseLong(parts[1]) );
//		break;
//		case SYMMETRIC: symmEdges = Long.parseLong(parts[1]);
//		System.out.println("symmEdges = " + symmEdges );
//		break;
//		case ORIGINAL: origEdges = Long.parseLong(parts[1]);
//		System.out.println("origEdges = " + origEdges );
//		break;
//		}
//	}
//	ds.close();
//}catch (Exception e){
//	System.err.println("Error: " + e.getMessage());
//}

//if (symmEdges == origEdges)
//	cmdSettings.setSymmetric( true );
//else
//	cmdSettings.setSymmetric( false );
