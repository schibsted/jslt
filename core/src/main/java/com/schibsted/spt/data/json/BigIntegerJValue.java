
package com.schibsted.spt.data.json;

import java.math.BigInteger;

public class BigIntegerJValue extends AbstractJsonValue {
  private BigInteger value;

  public BigIntegerJValue(BigInteger value) {
    this.value = value;
  }

  public boolean isNumber() {
    return true;
  }

  public boolean isIntegralNumber() {
    return true;
  }

  public int hashCode() {
    return value.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof BigIntegerJValue) {
      return ((BigIntegerJValue) other).value.equals(value);
    } else
      return false;
  }

  public String toString() {
    return value.toString();
  }
}
