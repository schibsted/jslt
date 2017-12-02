
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a "key" : <expr> pair inside a JSON object.
 */
public class PairExpression implements ExpressionNode {
  private String key;
  private ExpressionNode expr;

  public PairExpression(String key, ExpressionNode expr) {
    this.key = key;
    this.expr = expr;
  }

  public String getKey() {
    return key;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return expr.apply(scope, input);
  }

  public void dump(int level) {
  }
}
