import java.util.*;
import java.io.*;
import org.apache.commons.io.*;

/**
* Server class to run a database that implements the Database interface.
* Takes in user input on what to do and can add, retrieve, or delete files from
* three types of databases that utilize different chunking techniques
*/
public class Server {

  private static Database database; // actual database object

  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    boolean on = true;

    init(in);

    while (on) {
      System.out.println("Enter the number that represents what you want to do.");
      System.out.println("1: Insert a file into the data base");
      System.out.println("2: Retrieve a file in the data base");
      System.out.println("3: Delete a file from the data base");
      System.out.println("4: Show File List");
      System.out.println("5: Show Dedup Data");
      System.out.println("6: Clear Database");
      System.out.println("7: Quit");

      String decision = in.next();

      switch (decision) {
        case "1":
          add(getFile(in));
          break;
        case "2":
          retrieve(getFile(in));
          break;
        case "3":
          delete(getFile(in));
          break;
        case "4":
          getList();
          break;
        case "5":
          getData();
          break;
        case "6":
          clear();
          init(in);
          break;
        case "7":
          database.save();
          on = false;
          break;
        default:
        System.out.println("\nInvalid Input");
      }
    }
}

  public static void add(String file) {
    database.add(file);
  }

  public static void retrieve(String file) {
    database.retrieve(file);
  }

  public static void delete(String file) {
    database.delete(file);
  }

  public static void getData() {
    database.getData();
  }

  public static void getList() {
    database.getFileList();
  }

  private static String getFile(Scanner in) {
    System.out.println("Enter file name:");
    String file = in.next();
    return file;
  }

  /**
  * Initializes the server, databases and related files/folders
  */
  public static void init(Scanner in) {
    System.out.println("Initializing Database...");
    System.out.println("What type of chunking method do you want to use?");
    System.out.println("1: Whole File (No Chunking)");
    System.out.println("2: Fixed");
    System.out.println("3: Variable");

    File download = new File("Downloads");
    if (!download.exists()) {
      download.mkdir();
    }

    String chunkMethod = in.next();

    switch (chunkMethod) {
      case "1":
        initWhole();
        break;
      case "2":
        initFixed(in);
        break;
      case "3":
        initVariable(in);
        break;
      default:
      System.out.println("\nInvalid Input");
        init(in);
    }
  }

  private static void initWhole() {
    File temp = new File("WholeDatabase");
    if (!temp.exists()) {
      database = new WholeFileDatabase();
    } else {
      readDatabase("WholeDatabase");
      database = (WholeFileDatabase) database;
    }
  }

  private static void initFixed(Scanner in) {
    File temp = new File("/FixedChunkDatabase");
    if (!temp.exists()) {
      System.out.println("Enter chunk size in bytes:");
      int chunkSize = in.nextInt();
      database = new FixedChunkDatabase("/FixedChunkDatabase", chunkSize);
    } else {
        readDatabase("../FixedChunkDatabase");
        database = (FixedChunkDatabase) database;
    }
  }
  private static void initVariable(Scanner in) {
    File temp = new File("VariableDatabase");
    if (!temp.exists()) {
      System.out.println("Enter preferred average chunk size in bytes:");
      int chunkSize = in.nextInt();
      database = new VariableChunkingDatabase("VariableDatabase", chunkSize);
    } else {
      readDatabase("VariableDatabase");
      database = (VariableChunkingDatabase) database;
    }
  }

  // Method to deserialize database and read it into memory
  private static void readDatabase(String path) {
    try {
       FileInputStream fileIn = new FileInputStream(path + "/database.ser");
       ObjectInputStream inStream = new ObjectInputStream(fileIn);
       database = (Database) inStream.readObject();
       inStream.close();
       fileIn.close();
    } catch (IOException i) {
       i.printStackTrace();
    } catch (ClassNotFoundException c) {
       c.printStackTrace();
    }
  }

  private static void clear() {
    try {
      FileUtils.deleteDirectory(new File(database.getPath()));
      System.out.println("Databased Cleared\n");
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
}
