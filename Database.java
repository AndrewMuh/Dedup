import java.util.*;
import java.nio.file.*;
import org.apache.commons.io.*;
import java.io.*;

/**
* Database interface to be used in the Server. Only need 4 methods to implement
*/
public interface Database {
  public void add(String file); // adds file to database
  public void retrieve(String file); // clones file to disk from database
  public void delete(String file); // delete file from database
  public void save(); // save database to disk
  public void getFileList(); // print all files in database
  public void getData(); // print Dedup ratio, and bytes saved/deduped
  public String getPath(); // return database folder path
}
