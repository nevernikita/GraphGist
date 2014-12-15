-- Danai  3/14/2014 --

-----------------------------------------------
CoNtEnTs
-----------------------------------------------
1. Requirements
2. To Do
3. How to run
4. Possible runtime erros and solutions
5. Supported operations
-----------------------------------------------



*** 1. REQUIREMENTS ***

0) Java installed - type `which java`. If you don't get anything back, then it is not installed in your system.
1) Hadoop: To install Hadoop, navigate to the folder hadoop-0.20.203.0 run the hadoop-0.20.203.0/setEnv.sh script. If you get some error for hadoop, download it from the internet, and install it using setEnv.sh.

>> GIVEN version of HADOOP (hadoop-0.20.203.0)
   cd Path/to/trunk/static_graphs/hadoop-0.20.203.0
   bash setEnv.sh
   exit
   bash

 To chek the installation, type
   hadoop
 If you get hadoop options, then everything is set! :D
 If not, talk to me. Some parameters are not set properly...


>> If you want to download a NEW VERSION of HADOOP, download hadoop-2.3.0 and run setEnv_newHadoopVersion.sh in the parent directory where you've saved hadoop-2.3.0/.
   cd PARENT_DIR_OF_HADOOP          % should be something like: Path/to/trunk/static_graphs
   bash setEnv_newHadoopVersion.sh
   exit
   bash              % Important!!! you cannot run hadoop if you are not using bash shell.

 To chek the installation, type
   hadoop
 If you get hadoop options, then everything is set! :D
 If not, talk to me. Some parameters are not set properly...


2) Gnuplot: Version 4.2 patchlevel 6 (tested). Should work with a more recent version.
3) Eclipse
4) Hadoop plugin for eclipse (already in hadoop-0.20.203.0/hadoop-0.20.203.0/src/contrib/eclipse-plugin) 
5) Link the Jama library to the eclipse project (Perseus/Jama-1.0.2.jar).
6) Link the library hadoop-0.20.203.0/hadoop-0.20.203.0/lib/commons-cli-1.2.jar to the eclipse project.
7) sqlite3 (maybe in the future)



*** 2. TO DO ***

For the tasks 0-3, check first the Perseus.java file in GraphGist_v2/src/perseus. From there you might need to explore more src files.

0) Check the known issues listed at the beginning of Perseus.java.
1) Populate the database within each class of graphs - so that the appropriate types of info are loaded. Check lines 119-123 in Perseus.java.
  The idea is that if everything is stored in a database, we can use whatever statistics are needed to perform anomaly detection.
2) Add code to plot some more 1-d plots (e.g., pagerank, eigenvalues etc). (see "PDF" in ./plots_to_do.jpeg)
3) Add code to plot pairwise distributions of statistics.  (see X marks in ./plots_to_do.jpeg)
4) Unit tests
5) Add code (e.g., LOF, G-FADD, NetRay) for anomaly detection.
6) Use the visualization component to inspect the anomalies found. Check the folder 




*** 3. HOW-TO RUN ***

To run Perseus: 
1) load the project GraphGist_v2 in eclipse and 
2) run Perseus.java as Java application with command line arguments:
       -g <edgeFilename> <more options (see below)>

    e.g., -g catepillar_star_symm.edge -q rwr_query -debug

  To only create the plots, after having computed the statistics:
    e.g..   -g catepillar_star_symm.edge -q rwr_query -debug -onlyPlots

Outputs:    
Most outputs (text files with the distributions of the statistics) are saved in a folder named <edgeFilename> (the input argument that follows "-g", from which the ending has been stripped).


 ** Explanation of input arguments  **

    The following option is required: -graphPath, -graph, -g 
    Usage: <main class> [options] 
    Options: 
    
    -IterPR, -ipr 
    Number of iterations for PR 
    Default: 20 
    
    -block, -b 
    Block width 
    Default: 16 
    
    -debug 
    Debug mode 
    Default: false 
    
    -encode, -enc 
    enc or noenc 
    Default: false 
    
    -evals, -k 
    Number of evals/singular values 
    Default: 10 
    
    * -graphPath, -graph, -g 
    HDFS edge file path 
    
    -iterations, -m, -iter 
    Number of iterations for eig-decomposition/SVD 
    Default: 20 
    
    -mixingComp, -c 
    Mixing component 
    Default: 0.85 
    
    -nodes, -n 
    Number of nodes 
    Default: 0 
    
    -onlyPlots 
    Create the plots only 
    Default: false 
    
    -queryPath, -query, -q 
    HDFS directory containing query nodes 
    Default: rwr_query 
    
    -reducers, -r 
    Number of reducers 
    Default: 3 
    
    -symmetric, -symm 
    Sym/non-symmetric 
    Default: false 
    
    -weighted, -w 
    Weighted Graph 
    Default: false



*** 4. POSSIBLE RUNTIME ERRORS AND SOLUTIONS ***

PROBLEM 1:
java.io.IOException: Target out_graphInfo_local/out_graphInfo is a directory
SOLUTION 1:
remove the directory and re-run the code

PROBLEM 2:
missing directory
SOLUTION 2:
check if the produced directory has slightly different name than the name that appears in the code.



*** 5. SUPPORTED OPERATIONS ***

1) Find the number of nodes and edges 
2) Find if it is symmetric
3) Support computation of statistics for (i) Plain, (ii) Weighted, (iii) Directed and (iv) Weighted-Directed graphs.
   
  (i) Statistics for plain graphs:
    (a) node degree
    (b) Pagerank
    (c) Radius
    (d) Connected Components
    (e) PCA
    (f) Triangles

  (ii) Statistics for weighted graphs:
    (a) - (f)
    (g) weighted node degree
   
  (iii) Statistics for directed graphs:
    (a) - (f) 
    (h) node in-degree
    (i) node out-degree
    (j) directed pagerank
    (k) directed radius
    (l) SVD

  (iv) Statistics for weighted, directed graphs:
    (a) - (g)
    (m) weighted node in-degree
    (n) weighted node out-degree

4) Scripts for putting all the statistics in an sqlite3 database and plotting (need to be fixed)
	Check the *.bash and *.sql scripts in the Perseus/ directory.

