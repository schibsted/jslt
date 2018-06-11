
package com.schibsted.spt.data.jslt.impl;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Shared abstract superclass for comparison operators and others.
 */
public abstract class AbstractOperator extends AbstractNode {
  protected ExpressionNode left;
  protected ExpressionNode right;
  private String operator;

  public AbstractOperator(ExpressionNode left, ExpressionNode right,
                          String operator, Location location) {
    super(location);
    this.left = left;
    this.right = right;
    this.operator = operator;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode v1 = left.apply(scope, input);
    JsonNode v2 = right.apply(scope, input);
    return perform(v1, v2);
  }

  public void dump(int level) {
    left.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + operator);
    right.dump(level + 1);
  }

  public ExpressionNode optimize() {
    left = left.optimize();
    right = right.optimize();
    return this;
  }

  public abstract JsonNode perform(JsonNode v1, JsonNode v2);

  public String toString() {
    return left.toString() + " " + operator + " " + right;
  }
}
