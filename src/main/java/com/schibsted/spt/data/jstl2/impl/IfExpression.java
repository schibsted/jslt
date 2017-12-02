
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class IfExpression implements ExpressionNode {
  private ExpressionNode test;
  private LetExpression[] thenlets;
  private ExpressionNode then;
  private LetExpression[] elselets;
  private ExpressionNode orelse;

  public IfExpression(ExpressionNode test,
                      LetExpression[] thenlets,
                      ExpressionNode then,
                      LetExpression[] elselets,
                      ExpressionNode orelse) {
    this.test = test;
    this.thenlets = thenlets;
    this.then = then;
    this.elselets = elselets;
    this.orelse = orelse;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    if (isTrue(test.apply(scope, input)))
      return then.apply(NodeUtils.evalLets(scope, input, thenlets), input);

    // test was false, so return null or else
    if (orelse != null)
      return orelse.apply(NodeUtils.evalLets(scope, input, elselets), input);
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

  public void dump(int level) {
  }
}
