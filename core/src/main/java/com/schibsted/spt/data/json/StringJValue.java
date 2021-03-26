
package com.schibsted.spt.data.json;

public class StringJValue extends AbstractJsonValue {
  private String value;

  public StringJValue(String value) {
    this.value = value;
  }

  public boolean isSequence() {
    return true;
  }

  public boolean isString() {
    return true;
  }

  public String asString() {
    return value;
  }

  public int size() {
    return value.length();
  }

  public String toString() {
    return '"' + value + '"';
  }

  public int hashCode() {
    return value.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      return v.isString() && value.equals(v.asString());
    } else
      return false;
  }
}
