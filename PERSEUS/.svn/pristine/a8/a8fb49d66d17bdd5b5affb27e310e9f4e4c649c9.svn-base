import com.beust.jcommander.JCommander;



public class CallingCmdSettings {



	public enum Info { MAX, ORIGINAL, SYMMETRIC };


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		/* TO DO: some parsing of the graph                           */
		/* 3. renumber the nodes in the graph if not sequential?      */

		/* read the command line arguments */
		CmdSettings cmdSettings = new CmdSettings();
		new JCommander(cmdSettings, args);
		boolean debug = cmdSettings.getDebug();
		long symmEdges = -2;
		long origEdges = -1;

		System.out.println("****************************************");
		System.out.println("************* P E R S E U S ************");
		System.out.println("****************************************");
		System.out.println("* Input: " + cmdSettings.getGraphName());
		System.out.println("\t with nodes: " + cmdSettings.getNodes());
		System.out.println("* Outputs: 'outputs_" + cmdSettings.getGraphName() + "/'.");
		System.out.println("****************************************");

}
