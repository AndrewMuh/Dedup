import java.util.*;
import java.nio.file.*;
import org.apache.commons.io.*;
import java.io.*;

@SuppressWarnings("serial")
public class WholeFileDatabase implements Database, java.io.Serializable {
  private HashMap<String, HashSet<String>> database;
  private HashMap<String, String> reverseData;
  private double KBStored;
  private double KBDeduped;

  public WholeFileDatabase() {
    database = new HashMap<String, HashSet<String>>();
    reverseData = new HashMap<String, String>();
    KBStored = 0;
    KBDeduped = 0;

    File dataDir = new File("WholeDatabase");
    boolean res = dataDir.mkdir();
    if (!res) {
      System.out.println("Error creating Database!");
    }
  }
  public void add(String file) {
    File theFile = new File(file);
    String fileName = theFile.getName();
    try {
      byte[] bytes = Files.readAllBytes(theFile.toPath());

      Fingerprint fingerprint = new Fingerprint(bytes);

      String hashString = fingerprint.get();

      if (database.containsKey(hashString)) {
        HashSet<String> list = database.get(hashString);
        list.add(fileName);
        KBDeduped += bytes.length;
      } else {
        HashSet<String> list = new HashSet<String>();
        list.add(fileName);
        database.put(hashString, list);
        KBStored += bytes.length;
        FileUtils.writeByteArrayToFile(new File("WholeDatabase/" + hashString), bytes);
      }
      reverseData.put(fileName, hashString);
    } catch (IOException NoSuchFileException) {
        System.out.println("File does not exist!");
    } catch (Exception e) {
      System.out.println("Couldn't read all bytes");
      e.printStackTrace();
    }
  }

  public void retrieve(String file) {
    if (reverseData.containsKey(file)) {
      try {
        File old = new File("WholeDatabase/" + reverseData.get(file));
        File newFile = new File("Downloads/" + file);
        FileUtils.copyFile(old, newFile);
      } catch (Exception e) {
        System.out.println("Failed to retrieve file");
      }
    } else {
      System.out.println("File not found!");
    }
  }

  public void delete(String file) {
    if (reverseData.containsKey(file)) {
      String hash = reverseData.get(file);
      HashSet<String> set = database.get(hash);
      set.remove(file);
      File hashedFile = new File("WholeDatabase/" + hash);
      if (set.isEmpty()) {
        database.remove(hash);
        KBStored -= hashedFile.length();
        hashedFile.delete();
      } else {
        KBDeduped -= hashedFile.length();
      }
      reverseData.remove(file);
    }
  }

  public void save() {
    try {
     FileOutputStream saveData = new FileOutputStream("WholeDatabase/database.ser");
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
    Set<String> set = reverseData.keySet();
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
    System.out.println("Dedup Ratio = " + ((KBDeduped + KBStored) / (KBStored)) + "\n");
  }

  public String getPath() {
    return "WholeDatabase";
  }
}
