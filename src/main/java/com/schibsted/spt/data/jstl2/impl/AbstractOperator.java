
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

/**
 * Shared abstract superclass for comparison operators and others.
 */
public abstract class AbstractOperator extends AbstractNode {
  private ExpressionNode left;
  private ExpressionNode right;
  private String operator;

  public AbstractOperator(ExpressionNode left, ExpressionNode right,
                          String operator) {
    this.left = left;
    this.right = right;
    this.operator = operator;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode v1 = left.apply(scope, input);
    JsonNode v2 = right.apply(scope, input);
    return perform(v1, v2);
  }

  public void compile(Compiler compiler) {
    left.compile(compiler);
    right.compile(compiler);
    compiler.generateOperatorCode(operator);
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
}
