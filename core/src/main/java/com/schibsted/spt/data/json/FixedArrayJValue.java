
package com.schibsted.spt.data.json;

public class FixedArrayJValue extends AbstractJsonValue {
  private JsonValue[] array;
  private int size;

  public FixedArrayJValue(JsonValue[] array, int size) {
    this.array = array;
    this.size = size;
  }

  public boolean isSequence() {
    return true;
  }

  public boolean isArray() {
    return true;
  }

  public JsonValue get(int ix) {
    if (ix >= 0 && ix < array.length)
      return array[ix];
    return NullJValue.instance;
  }

  public int size() {
    return size;
  }

  public int hashCode() {
    throw new NullPointerException();
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      if (v.isArray() && v.size() == size()) {
        for (int ix = 0; ix < size; ix++) {
          JsonValue ov = v.get(ix);
          if (!ov.equals(array[ix]))
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
    for (int ix = 0; ix < size; ix++) {
      if (ix > 0)
        buf.append(",");
      buf.append(array[ix].toString());
    }
    buf.append("]");
    return buf.toString();
  }
}
