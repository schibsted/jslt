
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Expression;

public class IfExpression implements Expression {
  private Expression test;
  private Expression then;
  private Expression orelse;

  public IfExpression(Expression test, Expression then, Expression orelse) {
    this.test = test;
    this.then = then;
    this.orelse = orelse;
  }

  public JsonNode apply(JsonNode input) {
    if (isTrue(test.apply(input)))
      return then.apply(input);

    // test was false, so return null or else
    if (orelse != null)
      return orelse.apply(input);
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
