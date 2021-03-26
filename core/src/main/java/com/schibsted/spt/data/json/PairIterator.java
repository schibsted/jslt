
package com.schibsted.spt.data.json;

public interface PairIterator {

  public boolean hasNext();

  public String key();

  public JsonValue value();

  public void next();
}
