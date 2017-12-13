
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.JstlException;

public class AndOperator extends AbstractOperator {

  public AndOperator(ExpressionNode left, ExpressionNode right,
                     Location location) {
    super(left, right, "and", location);
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    boolean v1 = NodeUtils.isTrue(left.apply(scope, input));
    if (!v1)
      return BooleanNode.FALSE;

    boolean v2 = NodeUtils.isTrue(right.apply(scope, input));
    return NodeUtils.toJson(v1 && v2);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    throw new JstlException("Not implemented");
  }
}
