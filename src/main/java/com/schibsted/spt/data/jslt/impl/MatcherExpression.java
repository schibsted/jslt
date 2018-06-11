
package com.schibsted.spt.data.jslt.impl;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents the '* - ... : .'
 */
public class MatcherExpression extends AbstractNode {
  private List<String> minuses;
  private ExpressionNode expr;

  public MatcherExpression(ExpressionNode expr, List<String> minuses,
                           Location location) {
    super(location);
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

  public void dump(int level) {
  }
}
