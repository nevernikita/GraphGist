/***********************************************************************
    PEGASUS: Peta-Scale Graph Mining System
    Authors: U Kang, Duen Horng Chau, and Christos Faloutsos

This software is licensed under Apache License, Version 2.0 (the  "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-------------------------------------------------------------------------
File: EigenTriangle.java
 - Compute triangles
Version: 3.0
***********************************************************************/

package pegasus;

import Jama.*; 
import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class EigenTriangle extends Configured implements Tool 
{
    //////////////////////////////////////////////////////////////////////
    // command line interface
    //////////////////////////////////////////////////////////////////////

    // Main entry point.
    public static void main (final String[] args) throws Exception
    {
		final int result = ToolRunner.run(new Configuration(), new EigenTriangle(), args);

		return;
//		System.exit(result);
    }

    // Print the command-line usage text.
    protected static int printUsage ()
    {
		System.out.println("EigenTriangle <nreducer> <base_path_name>");

		ToolRunner.printGenericCommandUsage(System.out);

		return -1;
    }

	// submit the map/reduce job.
    public int run (final String[] args) throws Exception
    {
		if( args.length != 2 ) {
			return printUsage();
		}

		int nreducer = Integer.parseInt(args[0]);
		String base_path_name = "";
		if( args[1].equals("null") == false )
			base_path_name = args[1];

		System.out.println("\nRunning EigenTriangle... base_path_name=" + base_path_name);
		final FileSystem fs = FileSystem.get(getConf());

		String local_filename = "eigen.tri";

		// Load ritz.map
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream( "ritz.map" ), "UTF8"));
		FileWriter outfile = new FileWriter(local_filename);
		BufferedWriter out = new BufferedWriter (outfile);

		String cur_str = "";
		cur_str = in.readLine();
		if(cur_str == null )
			return -1;

		String[] tokens = cur_str.split("\t");
		int m = tokens.length;
		for(int i = 0; i < m; i++) {
			double eigval = Double.parseDouble(tokens[i]);
			double lam_cube = Math.pow(eigval, 3) ;

			out.write("" + (lam_cube/2) + "\t");
		}
		out.write("\n");
		in.close();
		out.close();

		// Compute triangles for each node
		Path rv_output = new Path("lz_tri");
		Path r_path = LinearAlgebraUtils.VecarrVecEigen( getConf(), m, nreducer, local_filename, base_path_name);

		fs.delete(rv_output);
		fs.rename(r_path, rv_output);

		System.out.println("EigenTriangle done. Triangle informations are saved in HDFS lz_tri. ");

		return 0;
    }
}

