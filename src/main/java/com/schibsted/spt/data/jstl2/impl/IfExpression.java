
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class IfExpression implements ExpressionNode {
  private ExpressionNode test;
  private ExpressionNode then;
  private ExpressionNode orelse;

  public IfExpression(ExpressionNode test, ExpressionNode then, ExpressionNode orelse) {
    this.test = test;
    this.then = then;
    this.orelse = orelse;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    if (isTrue(test.apply(scope, input)))
      return then.apply(scope, input);

    // test was false, so return null or else
    if (orelse != null)
      return orelse.apply(scope, input);
    else
      return NullNode.instance;
  }

  private boolean isTrue(JsonNode value) {
    return value != BooleanNode.FALSE &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isTextual() && value.asText().length() == 0) &&
      !(value.isArray() && value.size() == 0) &&
      !value.isNull();
  }

}
