
package com.schibsted.spt.data.json;

public interface JsonEventHandler {

  public void handleString(String value);

  public void handleLong(long value);

  public void handleDouble(double value);

  public void handleBoolean(boolean value);

  public void handleNull();

  public void startObject();

  public void handleKey(String key);

  public void endObject();

  public void startArray();

  public void endArray();

}
