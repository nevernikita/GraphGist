package perseus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pegasus.DegDist;

/** 
  Do 2 system calls in parallel.
  
  No time-out is used here.
*/
public final class ParallelSystemCalls {
  
  /** Run this tool. */
  public static final void main(String... aArgs) {
    ParallelSystemCalls checker = new ParallelSystemCalls();
    try {
      log("Parallel, report each as it completes:");
      checker.hadoopAndReportEachWhenKnown();
      
    }
    catch(InterruptedException ex){
      Thread.currentThread().interrupt();
    }
    catch(ExecutionException ex){
      log("Problem executing worker: " + ex.getCause());
    }
    log("Done.");
  }

  /** 
   Do 2 system calls, in parallel, using up to 2 threads. 
   Report the result of each 'ping' as it comes in. 
   (This is likely the style most would prefer.)
  */ 
  void hadoopAndReportEachWhenKnown()  throws InterruptedException, ExecutionException  {
    int numThreads = Commands.size() > 2 ? 2 : Commands.size(); //max 4 threads
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    CompletionService<Result> compService = new ExecutorCompletionService<Result>(executor);
    
    for(String cmd : Commands){
      Task task = new Task(cmd);
      compService.submit(task);
    }
    for(int idx = 0; idx < Commands.size(); ++idx){
      Future<Result> future = compService.take();
      log(future.get());
    }
    executor.shutdown(); //always reclaim resources
  }
  

  
  // PRIVATE 
//  String[] dd_args = new String[]{"catepillar_star.edge", "dd_node_deg", "dd_deg_count", "inout", "3"};
  
  private static final List<String> Commands = Arrays.asList(
	"DegDist.main(catepillar_star.edge dd_node_deg dd_deg_count inout 3)"
  );
  
  private static void log(Object aMsg){
    System.out.println(String.valueOf(aMsg));
  }
  
  /** Try to ping a URL. Return true only if successful. */
  private final class Task implements Callable<Result> {
    Task(String aCMD){
      fCMD = aCMD;
    }
    /** Access a URL, and see if you get a healthy response. */
    public Result call() throws Exception {
      return cmdAndReportStatus(fCMD);
    }
    private final String fCMD;
  }
  
  private Result cmdAndReportStatus(String aCMD) throws Exception {
    Result result = new Result();
    result.CMD = aCMD;
    long start = System.currentTimeMillis();
    String s = null;
    try {
      Process p = Runtime.getRuntime().exec(aCMD);
//      Process p = Runtime.getRuntime().exec(aCMD);
      p.waitFor();
//      String[] dd_args = new String[]{"catepillar_star.edge", "dd_node_deg", "dd_deg_count", "inout", "3"};
		
//      DegDist.main(dd_args);
      
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      while ((s = stdInput.readLine()) != null)
    	  result.OUTPUT = result.OUTPUT + "\n" + s ;  
      int errors = 0;
      while ((s = stdError.readLine()) != null){
    	  errors++;
    	  result.SUCCESS = false;
//    	  System.out.println(s);
		  result.ERROR = result.ERROR + "\n" + s ;
      }
      System.out.println("errors: " + errors);
      if ( errors == 0 )
    	  result.SUCCESS = true;
      
      long end = System.currentTimeMillis();
      result.TIMING = end - start;
    }
    catch(IOException ex){
      //ignore - fails
    }
    return result;
  }

  /** Hold all the data related to the command. */
  private final class Result {
    String CMD;
    String OUTPUT = new String();
    String ERROR = new String();
    Boolean SUCCESS;
    Long TIMING;
    @Override public String toString(){
      return "Result:" + SUCCESS + " " +TIMING + " msecs " + CMD + "\n OUTPUT: " + OUTPUT + "\n ERROR: " + ERROR;
    }
  }
} 
