
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class LetExpression implements ExpressionNode {
  private String variable;
  private ExpressionNode value;

  public LetExpression(String variable, ExpressionNode value) {
    this.variable = variable;
    this.value = value;
  }

  public String getVariable() {
    return variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value.apply(scope, input);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) +
                       "let " + variable + " = " + value.toString());
  }
}
