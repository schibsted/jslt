
package com.schibsted.spt.data.json;

import java.util.List;
import java.util.Iterator;

public class ListArrayJValue extends AbstractJsonValue {
  private List<JsonValue> array;

  public ListArrayJValue(List<JsonValue> array) {
    this.array = array;
  }

  public boolean isSequence() {
    return true;
  }

  public boolean isArray() {
    return true;
  }

  public JsonValue get(int ix) {
    if (ix >= 0 && ix < array.size())
      return array.get(ix);
    return NullJValue.instance;
  }

  public int size() {
    return array.size();
  }

  public Iterator<JsonValue> iterator() {
    return array.iterator();
  }

  public int hashCode() {
    return array.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      if (v.isArray() && v.size() == size()) {
        for (int ix = 0; ix < array.size(); ix++) {
          JsonValue ov = v.get(ix);
          if (!ov.equals(array.get(ix)))
            return false;
        }
        return true;
      }
      return false;
    } else
      return false;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("[");
    for (int ix = 0; ix < array.size(); ix++) {
      if (ix > 0)
        buf.append(',');
      buf.append(array.get(ix).toString());
    }
    buf.append("]");
    return buf.toString();
  }

  public void traverse(JsonEventHandler handler) {
    handler.startArray();
    for (int ix = 0; ix < array.size(); ix++)
      array.get(ix).traverse(handler);
    handler.endArray();
  }
}
