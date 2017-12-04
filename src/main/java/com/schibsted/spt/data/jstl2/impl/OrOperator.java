
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class OrOperator extends AbstractOperator {

  public OrOperator(ExpressionNode left, ExpressionNode right) {
    super(left, right, "or");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    return NodeUtils.toJson(NodeUtils.isTrue(v1) || NodeUtils.isTrue(v2));
  }
}
