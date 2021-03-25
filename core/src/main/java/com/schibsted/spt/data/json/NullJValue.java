
package com.schibsted.spt.data.json;

public class NullJValue extends AbstractJsonValue {
  public static NullJValue instance = new NullJValue();

  private NullJValue() {
  }

  public boolean isNull() {
    return true;
  }

  public String toString() {
    return "null";
  }

  public int hashCode() {
    return 0;
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      return v.isNull();
    } else
      return false;
  }
}
