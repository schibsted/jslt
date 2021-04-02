
package com.schibsted.spt.data.json;

public class BooleanJValue extends AbstractJsonValue {
  private boolean value;
  public static BooleanJValue FALSE = new BooleanJValue(false);
  public static BooleanJValue TRUE = new BooleanJValue(true);

  private BooleanJValue(boolean value) {
    this.value = value;
  }

  public boolean isBoolean() {
    return true;
  }

  public boolean asBoolean() {
    return value;
  }

  public String toString() {
    return value ? "true" : "false";
  }

  public int hashCode() {
    return value ? 1 : 2; // null == 0
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      if (v.isBoolean())
        return value == v.asBoolean();
      return false;
    } else
      return false;
  }

  public void traverse(JsonEventHandler handler) {
    handler.handleBoolean(value);
  }
}
