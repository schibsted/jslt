
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class AndOperator extends AbstractOperator {

  public AndOperator(ExpressionNode left, ExpressionNode right) {
    super(left, right, "and");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    return NodeUtils.toJson(NodeUtils.isTrue(v1) && NodeUtils.isTrue(v2));
  }
}
