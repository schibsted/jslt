
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class BiggerOrEqualComparison extends AbstractOperator {

  public BiggerOrEqualComparison(ExpressionNode left, ExpressionNode right,
                                 Location location) {
    super(left, right, ">=", location);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    // FIXME: taking a shortcut for now
    int n1 = NodeUtils.number(v1, location).intValue();
    int n2 = NodeUtils.number(v2, location).intValue();
    return NodeUtils.toJson(n1 >= n2);
  }

}
