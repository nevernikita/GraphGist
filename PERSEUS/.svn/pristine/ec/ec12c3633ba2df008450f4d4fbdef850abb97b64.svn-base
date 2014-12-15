package perseus;
import pegasus.WeightedDegDist;

/**
 * WeightedDirectedGraph.java: the class that contains all the additional
 *                             algorithms for mining *weighted*, directed graphs.
 */

/**
 * @author dkoutra
 *
 */
public class WeightedDirectedGraph extends DirectedGraph implements WeightedGraphInterface {

	/**
	 * @param edgeFile
	 */
	public WeightedDirectedGraph( String edgeFile, int reducers, long nodes, String graphName, String enc, String nosymOrmakesym, int evalsNo, boolean debug, int iterations ) {
		super( edgeFile, reducers, nodes, graphName, enc, nosymOrmakesym, evalsNo, debug, iterations );
		computeWeightedDirectedStatistics();
	}

	public void computeWeightedDirectedStatistics(){
		computeWeightedInOutDegree();
		if ( debug ) 
			System.out.println("Finished executing the weighted inout-Degree Distribution...");
		computeWeightedInDegree();
		if ( debug ) 
			System.out.println("Finished executing the weighted in-Degree Distribution...");
		computeWeightedOutDegree();
		if ( debug )
			System.out.println("Finished executing the weighted out-Degree Distribution...");
		
	}
	
	@Override
	public void computeWeightedInOutDegree() {
		String[] args = new String[]{edgeFile, "dd_node_deg_inout_weighted", "dd_deg_count_inout_weighted", "inout", Integer.toString(reducers)};
		try {
			WeightedDegDist.main(args);
			Perseus.moveFolders(outputFolder, "dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void computeWeightedInDegree() {
		String[] args = new String[]{edgeFile, "dd_node_deg_in_weighted", "dd_deg_count_in_weighted", "in", Integer.toString(reducers)};
		try {
			WeightedDegDist.main(args);
			Perseus.moveFolders(outputFolder, "dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void computeWeightedOutDegree() {
		String[] args = new String[]{edgeFile, "dd_node_deg_out_weighted", "dd_deg_count_out_weighted", "out", Integer.toString(reducers)};
		try {
			WeightedDegDist.main(args);
			Perseus.moveFolders(outputFolder, "dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
