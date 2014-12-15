package perseus;
/**
 * Interface with the graph analysis of a plain graph, 
 * i.e., undirected and unweighted.
 * @author dkoutra
 *
 */


public interface PlainGraphInterface {
	
	/* */
	public void computePlainStatistics();
	
	/* */
	public void computeInOutDegree();
	
	/* */
	public void computePagerank();
	
	/* */
	public void computeRadius();
	
	/* */
	public void computeConComp();
	
	/* */
	public void computeTriangles();
	
	/* */
	public void computePCA();
	
}
