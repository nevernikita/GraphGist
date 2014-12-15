import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;


public class Runner {
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		String run = "explore";		
		String tableName = "grid3";
//		String db = "./dummy.db";
		String db = "../ct_db_creator/ct.db";
		int i=0;		
		while (i < args.length) {
			if (args[i].equals("collapse")) 
				run = "collapse";			
			else if (args[i].equals("-table")) { 
				tableName = args[i+1];
				i++;
			} else if (args[i].equals("-db")) {
				db = args[i+1];
				i++;
			}				
			i++;
		}		
		if (run.equals("explore")) {
//			ArrayList<Integer> seedNodes = new ArrayList<Integer>();
			ArrayList<String> seedNodes = new ArrayList<String>();
//			for (int j = 0; j < 2; j++)
//				seedNodes.add(j);	
//			
			seedNodes.add("Insomnia ");
			seedNodes.add("HIV Infections ");
			InteractiveExploration ie = new InteractiveExploration(db,
					tableName);
//			ie.visualizeGraph(seedNodes);
			ie.vizGraph(seedNodes);
		} else if (run.equals("collapse")){
			JFrame f = new JFrame();
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        f.getContentPane().add(new VertexCollapse());
	        f.pack();
	        f.setVisible(true);
		} else {
			System.out.println("Valid options are 'explore' and 'collapse'");
		}
	}
}
