
package com.schibsted.spt.data.json;

import java.math.BigInteger;

public interface JsonEventHandler {

  public void handleString(String value);

  public void handleLong(long value);

  public void handleBigInteger(BigInteger value);

  public void handleDouble(double value);

  public void handleBoolean(boolean value);

  public void handleNull();

  public void startObject();

  public void handleKey(String key);

  public void endObject();

  public void startArray();

  public void endArray();

}
