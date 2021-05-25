import java.security.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class RabinHash implements java.io.Serializable {
  public static final int p = 1777; // prime number to use in hash algorithim
  public static final int q = 1097; // mod q that will be used to calculate the polynomial value
  public static int mask; // t bits of "1"s
  private int t; // t low order '0' bits to create chunk
  private int w; // sliding window size
  private byte[] file; // a file in byte form as a byte array
  private ArrayList<Integer> chunkIndex; // list of chunk breakpoint indices
  private int prevHash = 0; // last found hash, used to compute next hash
  private int currStart = 0; // current byte in the file
  private int currChunkSize = 0; // current size of the chunk
  private int maxChunkSize = 65536; // if currChunkSize >= maxChunkSize, a chunk breakpint will be made regardless of hashes


  /**
  * Constructor for RabinHash algorithm object
  */
  public RabinHash(byte[] file, int window, int lowBits) {
    t = lowBits;
    w = window;
    this.file = file;
    mask = 1 << t;
    mask--;
    chunkIndex = new ArrayList<Integer>();
  }

  /**
  * Method to chunk-ate a file and return chunk indices
  * @return chunkIndex ArrayList<Integer> of indices
  */
  public ArrayList<Integer> getChunks() {
    initHash(); // find first hash
    while (currStart < file.length) { // till we reach the end of the file
        if ((prevHash & mask) == 0 || currChunkSize == maxChunkSize) { // if low order t bits are 0 or if max chunkSize reached
          currStart += w - 1;
          chunkIndex.add(currStart); // adds breakpoint
          currChunkSize = 0;
          prevHash = 0;
          initHash(); // finds new first hash w bytes past last breakpoint
        }
        nextHash(); // compute next hash one byte over
    }
    chunkIndex.add(currStart); // adds final chunk
    return chunkIndex;
  }

  // compute next hash
  private void nextHash() {
    if (currStart + w - 1 >= file.length) {
      currStart = file.length;
      return;
    }
    prevHash = (prevHash * p - modPower(w) * file[currStart] + file[currStart + w - 1]) % q;
    currStart++;
  }

  // method to compute the first hash given a w sized window
  private void initHash() {
    int hash = 0;
    int i = currStart;
    int k = i + w - 1;
    if (file.length < k) {
      k = file.length - 1;
    }
    initHashRecursive(i, k);
    prevHash = hash;
  }

  // recursive method to compute hash mod q
  private int initHashRecursive(int i, int k) {
    currStart++;
    if (i == k) {
      return file[i];
    }
    return (p * initHashRecursive(i + 1, k) + file[i + 1]) % q;
  }

  // computes value of p^x mod q recursively
  private int modPower(int x) {
    if (x == 0) {
      return 1;
    }
    x--;
    return (p * modPower(x)) % q;
  }

}
