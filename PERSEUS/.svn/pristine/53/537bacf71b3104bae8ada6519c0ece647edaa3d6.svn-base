package perseus;
import java.io.*;

public class SystemCall {
	
	private static String cmd;
	
	public SystemCall( String cmd ){
		SystemCall.cmd = cmd;
	}

    public void execute() {

        String s = null;

        try {
            
        	System.out.println("The command we are trying to execute is:" + cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Standard output of the command:");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            
            // read any errors from the attempted command
            System.out.println("Standard error of the command (if any):");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            
            return;
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}