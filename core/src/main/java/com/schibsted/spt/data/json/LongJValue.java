
package com.schibsted.spt.data.json;

import com.schibsted.spt.data.jslt.JsltException;

public class LongJValue extends AbstractJsonValue {
  private long value;

  public LongJValue(long value) {
    this.value = value;
  }

  public long asLong() {
    return value;
  }

  public int asInt() {
    if (value < Integer.MAX_VALUE) // FIXME other bound
      return (int) value;
    else
      throw new JsltException("Long too large to return as int: " + value);
  }

  public double asDouble() {
    return (double) value;
  }

  public boolean isNumber() {
    return true;
  }

  public boolean isIntegralNumber() {
    return true;
  }

  public String toString() {
    return "" + value;
  }

  public int hashCode() {
    return (int) (value % Integer.MAX_VALUE);
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      if (v.isIntegralNumber())
        return value == v.asLong();
      return false;
    } else
      return false;
  }

  public void traverse(JsonEventHandler handler) {
    handler.handleLong(value);
  }
}
