
package com.schibsted.spt.data.json;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class DynamicJObject extends AbstractJsonValue implements JsonObjectBuilder {
  private Map<String, JsonValue> values;

  public DynamicJObject() {
    this.values = new HashMap();
  }

  public boolean isObject() {
    return true;
  }

  public int size() {
    return values.size();
  }

  public boolean has(String key) {
    return values.containsKey(key);
  }

  public JsonValue get(String key) {
    return values.get(key);
    // if (v == null)
    //   v = NullJValue.instance;
    // return v;
  }

  public Iterator<String> getKeys() {
    return values.keySet().iterator();
  }

  public PairIterator pairIterator() {
    return new DynamicPairIterator();
  }

  public int hashCode() {
    throw new NullPointerException();
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      return JValueUtils.equalObjects((JsonValue) other, this);
    } else
      return false;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("{");
    Iterator<Map.Entry<String, JsonValue>> it = values.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, JsonValue> entry = it.next();
      buf.append('"' + entry.getKey() + '"');
      buf.append(":");
      buf.append(entry.getValue().toString());

      if (it.hasNext())
        buf.append(",");
    }
    buf.append("}");
    return buf.toString();
  }

  // ===== JsonObjectBuilder

  public JsonObjectBuilder set(String key, JsonValue value) {
    values.put(key, value);
    return this;
  }

  public JsonObjectBuilder set(String key, String value) {
    values.put(key, makeValue(value));
    return this;
  }

  public JsonValue build() {
    return this;
  }

  // ===== PAIR ITERATOR

  class DynamicPairIterator implements PairIterator {
    private Iterator<Map.Entry<String,JsonValue>> iterator;
    private Map.Entry<String,JsonValue> entry;

    public DynamicPairIterator() {
      this.iterator = values.entrySet().iterator();
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public String key() {
      return entry.getKey();
    }

    public JsonValue value() {
      return entry.getValue();
    }

    public void next() {
      this.entry = iterator.next();
    }
  }
}
