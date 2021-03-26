
package com.schibsted.spt.data.json;

import java.util.Iterator;

public interface JsonValue extends JsonBuilder {

  // array or string
  public boolean isSequence();

  public boolean isString();

  public boolean isArray();

  public boolean isNumber();

  public boolean isNull();

  public boolean isIntegralNumber();

  public boolean isDecimalNumber();

  public boolean isObject();

  public boolean isBoolean();

  public String asString();

  public int asInt();

  public long asLong();

  public double asDouble();

  public boolean asBoolean();

  // has object key
  public boolean has(String key);

  // get object key
  public JsonValue get(String key);

  // get array value
  public JsonValue get(int ix);

  public int size();

  public Iterator<String> getKeys();

  public PairIterator pairIterator();

}
