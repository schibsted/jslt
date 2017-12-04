
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class UnequalsComparison extends AbstractOperator {

  public UnequalsComparison(ExpressionNode left, ExpressionNode right) {
    super(left, right, "!=");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    return NodeUtils.toJson(!v1.equals(v2));
  }

}
