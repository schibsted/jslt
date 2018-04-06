
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

/**
 * Represents a "key" : <expr> pair inside a JSON object.
 */
public class PairExpression extends AbstractNode {
  private String key;
  private ExpressionNode expr;

  public PairExpression(String key, ExpressionNode expr, Location location) {
    super(location);
    this.key = key;
    this.expr = expr;
  }

  public String getKey() {
    return key;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return expr.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    expr.computeMatchContexts(new DotExpression(key, parent, location));
  }

  public void compile(Compiler compiler) {
    expr.compile(compiler);
  }

  // is the expr a literal?
  public boolean isLiteral() {
    return expr instanceof LiteralExpression;
  }

  public ExpressionNode optimize() {
    expr = expr.optimize();
    return this;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + '"' + key + '"' + " :");
    expr.dump(level + 1);
  }
}
