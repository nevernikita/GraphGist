package pegasus;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;

public class OverwriteOutputDirOutputFormat extends TextOutputFormat {
 @Override
  public void checkOutputSpecs(FileSystem ignored, JobConf job)
  throws FileAlreadyExistsException,
         InvalidJobConfException, IOException {

       // bypass the output directory check.

   }
}
