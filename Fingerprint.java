import java.security.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;

@SuppressWarnings("serial")
/**
* Fingerprint class used to hash a given chunk. MD5 hashing is used here and the
* hash is stored as a hexadecimal string.
*/
public class Fingerprint implements java.io.Serializable {
  private String hash;

  public Fingerprint(byte[] chunk) throws Exception {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(chunk);
    byte[] hashArr = md.digest();
    StringBuilder builder = new StringBuilder();
    for(byte b : hashArr) { // converts to hexadecimal
      builder.append(String.format("%02x", b));
    }
    hash = builder.toString();
  }

  public Fingerprint(String hash) {
    this.hash = hash;
  }

  public String get() {
    return hash;
  }

  public boolean equals(Fingerprint f) {
    return hash.equals(f.get());
  }
}
