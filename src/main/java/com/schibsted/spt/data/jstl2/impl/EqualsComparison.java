
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

public class EqualsComparison implements ExpressionNode {
  private ExpressionNode left;
  private ExpressionNode right;

  public EqualsComparison(ExpressionNode left, ExpressionNode right) {
    this.left = left;
    this.right = right;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode v1 = left.apply(scope, input);
    JsonNode v2 = right.apply(scope, input);
    return NodeUtils.toJson(v1.equals(v2));
  }

  public void dump(int level) {
  }
}
