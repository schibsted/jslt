
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class LetExpression extends AbstractNode {
  private String variable;
  private ExpressionNode value;

  public LetExpression(String variable, ExpressionNode value, Location location) {
    super(location);
    this.variable = variable;
    this.value = value;
  }

  public String getVariable() {
    return variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    value.computeMatchContexts(parent);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) +
                       "let " + variable + " =");
    value.dump(level + 1);
  }

  public ExpressionNode optimize() {
    value = value.optimize();
    return this;
  }
}
