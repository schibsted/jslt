
package com.schibsted.spt.data.jslt.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.JsltException;

public class VariableExpression extends AbstractNode {
  private String variable;

  public VariableExpression(String variable, Location location) {
    super(location);
    this.variable = variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode value = scope.getValue(variable);
    if (value == null)
      throw new JsltException("No such variable '" + variable + "'",
                              location);
    return value;
  }

  public void dump(int level) {
  }

  public String toString() {
    return "$" + variable;
  }
}
