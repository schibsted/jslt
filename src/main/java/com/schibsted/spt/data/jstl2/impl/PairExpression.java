
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.Expression;

/**
 * Represents a "key" : <expr> pair inside a JSON object.
 */
public class PairExpression implements Expression {
  private String key;
  private Expression expr;

  public PairExpression(String key, Expression expr) {
    this.key = key;
    this.expr = expr;
  }

  public String getKey() {
    return key;
  }

  public JsonNode apply(JsonNode input) {
    return expr.apply(input);
  }

}
