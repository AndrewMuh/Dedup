import java.util.*;
import java.nio.file.*;
import org.apache.commons.io.*;
import java.io.*;

@SuppressWarnings("serial")

public class VariableChunkingDatabase extends FixedChunkDatabase implements Database, java.io.Serializable {
  /**
  * Note: 2^t - 1 + w = average chunk size for the RabinHash algorithim
  */
  private int t; // low order bits needed to be 0 before next chunk is determined
  private int w; // sliding window size

  public VariableChunkingDatabase(String dataPath, int chunkSize) {
    super(dataPath, 0); // instantiate super class with chunkSize 0 since chunkSize won't be used here

    t = 0; // Math to figure out t and w from given preferred chunkSize
    int res = 2;
    while ((res - 1) < chunkSize) {
      res *= 2;
      t++;
    }
    res /= 2;
    w = chunkSize - res + 1;
  }

  /**
  * Similar to FixedChunkDatabase add method except it uses RabinHash to chunk-ate a file
  * instead of using a fixed chunk size
  * @param file the file path
  */
  public void add(String file) {
    File theFile = new File(file);
    String fileName = theFile.getName();
    if (index.containsKey(fileName)) {
      System.out.println("File already in database!");
    }
    try {
      byte[] bytes = Files.readAllBytes(theFile.toPath());

      // Runs Rabin-Karp algorithim to create chunks in the file given the file as a byte array, window w, and bits t.
      RabinHash rabin = new RabinHash(bytes, w, t);

      ArrayList<Integer> chunkIndex = rabin.getChunks(); // Returns ArrayList of indices where chunks are seperated

      // Similar code to FixedChunking except with chunks given by chunkIndex[i+1] - chunkInde[i];
      ArrayList<Fingerprint> printList = new ArrayList<Fingerprint>();
      int start = 0;
      for (Integer i : chunkIndex) {
        byte[] chunk = Arrays.copyOfRange(bytes, start, i);
        start = i;
        Fingerprint fingerprint = new Fingerprint(chunk);
        printList.add(fingerprint);
        String hash = fingerprint.get();
        if (!fingerprintIndex.contains(hash)) {
          reverseIndex.put(hash, new HashSet<String>());
          KBStored += chunk.length / 1024.00;
          fingerprintIndex.add(hash);
          FileUtils.writeByteArrayToFile(new File(databasePath + "/" + hash), chunk);
        } else {
          HashSet<String> set = reverseIndex.get(hash);
          set.add(file);
          KBDeduped += chunk.length / 1024.00;
        }
      }
      index.put(fileName, printList);
    } catch (IOException NoSuchFileException) {
        System.out.println("File does not exist!");
    } catch (Exception e) {
      System.out.println("Couldn't read all bytes");
      e.printStackTrace();
    }
  }
}
