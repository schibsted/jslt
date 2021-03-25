
package com.schibsted.spt.data.json;

public class DoubleJValue extends AbstractJsonValue {
  private double value;

  public DoubleJValue(double value) {
    this.value = value;
  }

  public boolean isNumber() {
    return true;
  }

  public boolean isDecimalNumber() {
    return true;
  }

  public double asDouble() {
    return value;
  }

  public String toString() {
    return "" + value;
  }

  public int hashCode() {
    if (value > 1)
      return (int) (value * 100);
    else
      return (int) (value * Integer.MAX_VALUE);
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      if (v.isDecimalNumber())
        return value == v.asDouble();
      return false;
    } else
      return false;
  }
}
