
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class VariableExpression implements ExpressionNode {
  private String variable;

  public VariableExpression(String variable) {
    this.variable = variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return scope.getValue(variable);
  }

  public void dump(int level) {
  }
}
