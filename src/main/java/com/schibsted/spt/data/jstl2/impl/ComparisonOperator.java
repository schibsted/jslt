
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.JstlException;

public abstract class ComparisonOperator extends AbstractOperator {

  public ComparisonOperator(ExpressionNode left, ExpressionNode right,
                            String operator, Location location) {
    super(left, right, operator, location);
  }

  public abstract JsonNode perform(JsonNode v1, JsonNode v2);

  public double compare(JsonNode v1, JsonNode v2) {
    if (v1.isNumber() && v2.isNumber()) {
      double n1 = NodeUtils.number(v1, location).doubleValue();
      double n2 = NodeUtils.number(v2, location).doubleValue();
      return n1 - n2;

    } else if (v1.isTextual() && v2.isTextual()) {
      String s1 = v1.asText();
      String s2 = v2.asText();
      return (double) s1.compareTo(s2);

    }

    throw new JstlException("Can't compare " + v1 + " and " + v2);
  }

}
