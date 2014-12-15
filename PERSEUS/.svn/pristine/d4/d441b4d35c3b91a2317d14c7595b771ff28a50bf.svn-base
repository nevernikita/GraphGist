import com.beust.jcommander.Parameter;

/* Reminder: you can put all the parameters in a file and call java Main @path/to/file/with/parameters */

public class CmdSettings {

	@Parameter(names = {"-graphPath", "-graph", "-g"}, description = "HDFS edge file path", required = true)
	private  String graphEdgeFile;
	
	@Parameter(names = {"-symmetric", "-symm"}, description = "Sym/non-symmetric", arity = 1)
	private boolean symmetric = false;
	
	@Parameter(names = {"-reducers", "-r"}, description = "Number of reducers", arity = 1)
	private int reducers = 3;
	
	@Parameter(names = {"-nodes", "-n"}, description = "Number of nodes", arity = 1)
	private long nodesNo;
	
	@Parameter(names = {"-encode", "-enc"}, description = "enc or noenc", arity = 1)
	private boolean encode = false;
	
	@Parameter(names = {"-iterations", "-m", "-iter"}, description = "Number of iterations for eig-decomposition/SVD", arity = 1)
	private int iterNo = 20;
	
	@Parameter(names = {"-evals", "-k"}, description = "Number of evals/singular values", arity = 1)
	private int evalsNo = 6;
	
	@Parameter(names = {"-queryPath", "-query", "-q"}, description = "HDFS directory containing query nodes", arity = 1)
	private String queryPath = "rwr_query";
	
	@Parameter(names = {"-block", "-b"}, description = "Block width", arity = 1)
	private int blockWidth = 16;
	
	@Parameter(names = {"-mixingComp", "-c"}, description = "Mixing component", arity = 1)
	private double c = 0.85;
	
	@Parameter(names = {"-IterPR", "-ipr"}, description = "Number of iterations for PR", arity = 1)
	private int iterPR = 20; //1024
	
	@Parameter(names = "-debug", description = "Debug mode")
	private boolean debug = false;

	@Parameter(names = "-onlyPlots", description = "Create the plots only")
	private boolean onlyPlots = false;
	
	@Parameter(names = {"-weighted", "-w"}, description = "Weighted Graph")
	private boolean weightedGraph = false;
	
	/* getters */

	public String getGraphEdgeFile() {
		return graphEdgeFile;
	}
	
	public String getGraphName() {
		String[] parts = graphEdgeFile.split("\\.");		
		return parts[0];
	}
	
	public String getSymmetric() {
		// if symmetric, run each algorithm on the original edge file only
		// else, run the algorithms on the initial (directed) graph, and also on the symmetric/undirected graph
		if (symmetric == true) 
			return "nosym";
		else
			return "makesym";
	}
	
	public void setSymmetric( Boolean isSymm ) {
		symmetric = isSymm; 
	}
	
	public int getReducers() {
		return reducers;
	}
	
	public long getNodes() {
		return nodesNo;
	}
	
	public void setNodes( long nodes ) {
		nodesNo = nodes;
	}

	public String getEncode() {
		if (encode == true) 
			return "enc";
		else
			return "noenc";
	}
	
	public int getIterNo() {
		return iterNo;
	}
	
	public int getEvals() {
		return evalsNo;
	}
	
	public String getQueryPath() {
		return queryPath;
	}
	
	public int getBlock() {
		return blockWidth;
	}
	
	public double getMixingComp() {
		return c;
	}
	
	public int getIterPageRank(){
		return iterPR;
	}
	
	public boolean getDebug(){
		return debug;
	}

	public boolean isOnlyPlots() {
		return onlyPlots;
	}

	public boolean getWeightedGraph() {
		return weightedGraph;
	}
	
	
}
