package perseus;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import pegasus.SymmetricAndNumberOfNodes;


public class Graph {

	public enum Info { MAX, ORIGINAL, SYMMETRIC };

	
	String graphFile;
	int reducers;
	long nodesNo;
	boolean isSymmetric;
	CmdSettings cmdSettings;
	long symmEdges = -2;
	long origEdges = -1;
	
	// constructor
	public Graph( String name, int reducers, CmdSettings cmdSettings ){
		graphFile = name;
		this.reducers = reducers;
		this.cmdSettings = cmdSettings;
		System.out.println("Finding the number of nodes in '" + graphFile + "', as well as if it is symmetric.");
		findNodesAndSymmetry();
		setNodesNo();
		setSymmetric();
	}
	
	/* find the number of nodes in the graph + if it is symmetric or not */
	private void findNodesAndSymmetry(){

		String[] symmAndNodesNo_args = new String[]{ graphFile, "out_graphInfo", Integer.toString(reducers) };
		try {
			SymmetricAndNumberOfNodes.main(symmAndNodesNo_args);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		/* read output file with info about the symmetry of the adjacency matrix + 
		 * the number of nodes */
		try{
			FileInputStream fs = new FileInputStream("out_graphInfo/part-00000");
			DataInputStream ds = new DataInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(ds));
			String strLine;
			while (( strLine = br.readLine()) != null){
				String[] parts = strLine.split("\t");

				Info info = Info.valueOf( parts[0].toUpperCase() );
				switch ( info  ){
				case MAX: nodesNo = Long.parseLong(parts[1]);
				break;
				case SYMMETRIC: symmEdges = Long.parseLong(parts[1]);
				System.out.println("symmEdges = " + symmEdges );
				break;
				case ORIGINAL: origEdges = Long.parseLong(parts[1]);
				System.out.println("origEdges = " + origEdges );
				break;
				}
			}
			ds.close();
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	private void setNodesNo(){
		cmdSettings.setNodes( nodesNo );
	}
	
	
	private void setSymmetric(){
		if (symmEdges == origEdges)
			cmdSettings.setSymmetric( true );
		else
			cmdSettings.setSymmetric( false );
	}
	
	
	
	
	
	
}
