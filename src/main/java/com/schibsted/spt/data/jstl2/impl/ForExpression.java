
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jstl2.JstlException;

public class ForExpression extends AbstractNode {
  private ExpressionNode valueExpr;
  private ExpressionNode loopExpr;

  public ForExpression(ExpressionNode valueExpr,
                      ExpressionNode loopExpr) {
    this.valueExpr = valueExpr;
    this.loopExpr = loopExpr;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode array = valueExpr.apply(scope, input);
    if (array.isNull())
      return NullNode.instance;
    if (!array.isArray())
      throw new JstlException("For loop can't iterate over " + array);

    ArrayNode result = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < array.size(); ix++) {
      JsonNode value = array.get(ix);
      result.add(loopExpr.apply(scope, value));
    }
    return result;
  }

  public void computeMatchContexts(DotExpression parent) {
    // if you do matching inside a for the matching is on the current
    // object being traversed in the list. so we forget the parent
    // and start over
    loopExpr.computeMatchContexts(new DotExpression());
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + "for (");
    valueExpr.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ")");
    loopExpr.dump(level + 1);
  }
}
