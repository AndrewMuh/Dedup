import java.util.*;
import java.nio.file.*;
import org.apache.commons.io.*;
import java.io.*;

@SuppressWarnings("serial")
public class FixedChunkDatabase implements Database, java.io.Serializable {
  // Chunk Size in bytes
  private final int chunkSize;

  protected double KBStored;
  protected double KBDeduped;

  // Index representing a mapping of file names and a fingerprint list that make
  // up the file
  protected HashMap<String, ArrayList<Fingerprint>> index;

  // An index that contains the name of every file that contains a spcific chunk
  protected HashMap<String, HashSet<String>> reverseIndex;

  // A set of all the fingerprints in the database
  protected HashSet<String> fingerprintIndex;

  // A string that stores the path of the database
  protected String databasePath;

  /**
  * Constructor for a FixedChunkDatabase
  * @param dataPath the path of the database to make
  * @param sizeBytes preferred chunk size
  */
  public FixedChunkDatabase(String dataPath, int sizeBytes) {
    chunkSize = sizeBytes;
    index = new HashMap<String, ArrayList<Fingerprint>>();
    fingerprintIndex = new HashSet<String>();
    reverseIndex = new HashMap<String, HashSet<String>>();
    databasePath = dataPath;
    KBStored = 0;
    KBDeduped = 0;

    File dataDir = new File(databasePath);
    boolean res = dataDir.mkdir();
    if (!res) {
      System.out.println("Error creating Database!");
    }
  }

  /**
  * Adds a file to the Database by chunking it and storing the chunks as files and their hashes
  * in the HashMap.
  * @param file the file path of the file being added
  */
  public void add(String file) {
    File theFile = new File(file);
    String fileName = theFile.getName();
    if (index.containsKey(theFile)) { // check if file is in database
      System.out.println("File already in database!");
      return;
    }
    try {
      byte[] bytes = Files.readAllBytes(theFile.toPath()); // read in file to memory as a byte array

      ArrayList<Fingerprint> printList = new ArrayList<Fingerprint>(); // list of hash fingerprints

      int i = 0; // counter for bytes in the file
      int chunkS = chunkSize; // temporary variable incase we reach end of array
      while (i < bytes.length) {
        if (i + chunkS >= bytes.length) {
          chunkS = bytes.length - i;
        }
        byte[] chunk = Arrays.copyOfRange(bytes, i, i + chunkS); // seperate a chunk of size chunkSize from file bytes array
        Fingerprint fingerprint = new Fingerprint(chunk); // fingerprint this chunk
        printList.add(fingerprint); // add this fingerprint to the list for this file name
        String hash = fingerprint.get(); // retrieve the hash string of the fingerprint
        if (!fingerprintIndex.contains(hash)) { // check if this fingerprint is in database already
          reverseIndex.put(hash, new HashSet<String>()); // if not add it
          KBStored += chunk.length / 1024.00;
          fingerprintIndex.add(hash);
          FileUtils.writeByteArrayToFile(new File(databasePath + "/"+ hash), chunk); // write chunk with the hash as file name
        } else {
          HashSet<String> set = reverseIndex.get(hash); // fingerprint is already on disk, no need to write
          set.add(file);
          KBDeduped += chunk.length / 1024.00;
        }
        i += chunkSize;
      }
      index.put(fileName, printList); // add this file to database with its fingerprint list
    } catch (IOException NoSuchFileException) {
        System.out.println("File does not exist!");
    } catch (Exception e) {
      System.out.println("Couldn't read all bytes");
      e.printStackTrace();
    }
  }

  /**
  * Retrieves file from database by piecing together chunks by their fingerprints
  * @param file the file path
  */
  public void retrieve(String file) {
    if (index.containsKey(file)) { // check if file exists in database
      ArrayList<Fingerprint> list = index.get(file); // get the fingerprints associated with file
      try {
        File outputFile = new File("Downloads/" + file);
        outputFile.createNewFile(); // creates the file clone from database
        FileOutputStream output = new FileOutputStream(outputFile);
        for (Fingerprint fi : list) { // pulls each fingerprint and writes their data to clone
          String hash = fi.get();
          File chunk = new File(databasePath + "/" + hash);
          FileUtils.copyFile(chunk, output);
        }
        output.close();
      } catch (Exception e) {
        System.out.println("Failed to retrieve file");
      }
    } else {
      System.out.println("File not found!");
    }
  }

  /**
  * Delete file from database and associated fingerprints if not used by other files
  * @param file file path
  */
  public void delete(String file) {
    if (index.containsKey(file)) { // check if file exists in database
      ArrayList<Fingerprint> list = index.get(file);
      for (Fingerprint fi : list) { // remove file from fingerprint's file list
        String hash = fi.get();
        HashSet<String> set = reverseIndex.get(hash);
        set.remove(file);
        File hashFile = new File(databasePath + "/" + fi.get());
        if (set.size() == 0) { // if no files are associated with fingerprint, delete fingerprint
          reverseIndex.remove(hash);
          fingerprintIndex.remove(hash);
          hashFile.delete();
          KBStored -= hashFile.length() / 1024.00;
        } else {
          KBDeduped -= hashFile.length() / 1024.00;
        }
      }
      index.remove(file); // remove from database
    }
  }

  /**
  * Serialize and write this object to disk to save the index
  */
  public void save() {
    try {
     FileOutputStream saveData = new FileOutputStream(databasePath + "/database.ser");
     ObjectOutputStream out = new ObjectOutputStream(saveData);
     out.writeObject(this);
     out.close();
     saveData.close();
     System.out.printf("Serialized data is saved");
    } catch (IOException i) {
      i.printStackTrace();
    }
  }

  public void getFileList() {
    Set<String> set = index.keySet();
    Iterator<String> iter = set.iterator();

    System.out.println("\nFile List:\n");
    while (iter.hasNext()) {
      System.out.println(iter.next());
    }
    System.out.println("");
  }

  public void getData() {
    System.out.println("KB Stored: " + KBStored);
    System.out.println("KB Deduped: " + KBDeduped);
    System.out.println("Dedup Ratio = " + (KBDeduped / (KBStored + KBDeduped)) + "\n");
  }

  public String getPath() {
    return databasePath;
  }
}
