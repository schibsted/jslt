
package com.schibsted.spt.data.jslt.vm;

import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

// this should be an interface, so different kinds of managers can be used
public class ResourceManager {
  private Map<String, Integer> reverseLexicon;
  private String[] lexicon;

  private Map<BigInteger, Integer> reverseBigInt;
  private BigInteger[] bigInts;

  public ResourceManager() {
    this.lexicon = new String[1024];
    this.reverseLexicon = new HashMap<>();
    this.bigInts = new BigInteger[1024];
    this.reverseBigInt = new HashMap<>();
  }

  public int getStringId(String str) {
    Integer id = reverseLexicon.get(str);
    if (id == null) {
      id = new Integer(reverseLexicon.size());
      if (lexicon.length == id)
        expandLexicon();
      reverseLexicon.put(str, id);
      lexicon[id] = str;
    }
    return id;
  }

  public String getString(int id) {
    return lexicon[id];
  }

  public int getBigIntId(BigInteger bigint) {
    Integer id = reverseLexicon.get(bigint);
    if (id == null) {
      id = new Integer(reverseBigInt.size());
      if (bigInts.length == id)
        expandBigInts();
      reverseBigInt.put(bigint, id);
      bigInts[id] = bigint;
    }
    return id;
  }

  public BigInteger getBigInt(int id) {
    return bigInts[id];
  }

  private void expandLexicon() {
    String[] newLexicon = new String[lexicon.length * 2];
    System.arraycopy(lexicon, 0, newLexicon, 0, lexicon.length);
    lexicon = newLexicon;
  }

  private void expandBigInts() {
    BigInteger[] newBigInts = new BigInteger[bigInts.length * 2];
    System.arraycopy(bigInts, 0, newBigInts, 0, bigInts.length);
    bigInts = newBigInts;
  }
}
