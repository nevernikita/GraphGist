package perseus;
import pegasus.DegDist;
import pegasus.WeightedDegDist;

/**
 * WeightedGraph.java: the class that contains an additional algorithm
 *                     for mining undirected, weighted graphs.
 */

/**
 * @author dkoutra
 *
 */
public class WeightedGraph extends PlainGraph {

	/**
	 * @param edgeFile
	 */
	public WeightedGraph( String edgeFile, int reducers, long nodes, String graphName, String enc, String nosymOrmakesym, int evalsNo, boolean debug, int iterations ) {
		super( edgeFile, reducers, nodes, graphName, enc, nosymOrmakesym, evalsNo, debug, iterations );
		computeWeightedStatistics();
	}
	
	public void computeWeightedStatistics(){
		computeWeightedInoutDegree();
		if ( debug ) 
			System.out.println("Finished executing the weighted inOut-Degree Distribution...");
	}
	
	private void computeWeightedInoutDegree(){
		String[] args = new String[]{edgeFile, "dd_node_deg_inout_weighted", "dd_deg_count_inout_weighted", "inout", Integer.toString(reducers)};
		try {
			WeightedDegDist.main(args);
			Perseus.moveFolders(outputFolder, "dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
