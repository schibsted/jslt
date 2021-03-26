
package com.schibsted.spt.data.json;

import java.util.Iterator;
import java.util.Collections;

public class EmptyJObject extends AbstractJsonValue {
  public static final EmptyJObject EMPTY_OBJECT = new EmptyJObject();

  private EmptyJObject() {
  }

  public boolean isObject() {
    return true;
  }

  public int size() {
    return 0;
  }

  public boolean has(String key) {
    return false;
  }

  public JsonValue get(String key) {
    return null;
  }

  public Iterator<String> getKeys() {
    return Collections.emptyIterator();
  }

  public PairIterator pairIterator() {
    return AbstractJsonValue.EMPTY_PAIR_ITERATOR;
  }

  public int hashCode() {
    return 0;
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      return v.isObject() && v.size() == 0;
    } else
      return false;
  }

  public String toString() {
    return "{}";
  }
}
