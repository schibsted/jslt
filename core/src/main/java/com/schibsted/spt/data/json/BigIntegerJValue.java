
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
}
