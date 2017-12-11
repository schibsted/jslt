
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class EqualsComparison extends AbstractOperator {

  public EqualsComparison(ExpressionNode left, ExpressionNode right) {
    super(left, right, "==");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    boolean result;
    if (v1.isNumber() && v2.isNumber()) {
      // unfortunately, comparison of numeric nodes in Jackson is
      // deliberately less helpful than what we need here. so we have
      // to develop our own support for it.
      // https://github.com/FasterXML/jackson-databind/issues/1758

      if (v1.isIntegralNumber() && v2.isIntegralNumber())
        // if both are integers, then compare them as such
        result = v1.longValue() == v2.longValue();
      else
        result = v1.doubleValue() == v2.doubleValue();
    } else
      result = v1.equals(v2);
    return NodeUtils.toJson(result);
  }

}
