
package com.schibsted.spt.data.json;

public interface JsonObjectBuilder {

  public JsonObjectBuilder set(String key, JsonValue value);

  public JsonObjectBuilder set(String key, String value);

  public boolean has(String key);

  public JsonValue build();

}
