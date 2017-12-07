
package com.schibsted.spt.data.jstl2.impl;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

/**
 * Represents the '* - ... : .'
 */
public class MatcherExpression extends AbstractNode {
  private List<String> minuses;
  private ExpressionNode expr;

  public MatcherExpression(ExpressionNode expr, List<String> minuses) {
    this.minuses = minuses;
    this.expr = expr;
  }

  public List<String> getMinuses() {
    return minuses;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return expr.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    // FIXME: uhhh, the rules here?
  }

  public void compile(Compiler compiler) {
    expr.compile(compiler);
  }

  public void dump(int level) {
  }
}
