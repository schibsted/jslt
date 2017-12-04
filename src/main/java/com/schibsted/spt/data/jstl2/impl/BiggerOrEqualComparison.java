
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class BiggerOrEqualComparison extends AbstractOperator {

  public BiggerOrEqualComparison(ExpressionNode left, ExpressionNode right) {
    super(left, right, ">=");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    // FIXME: taking a shortcut for now
    int n1 = NodeUtils.number(v1).intValue();
    int n2 = NodeUtils.number(v2).intValue();
    return NodeUtils.toJson(n1 >= n2);
  }

}
