
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ArrayExpression extends AbstractNode {
  private ExpressionNode[] children;

  public ArrayExpression(ExpressionNode[] children, Location location) {
    super(location);
    this.children = children;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    ArrayNode array = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < children.length; ix++)
      array.add(children[ix].apply(scope, input));
    return array;
  }

  public void computeMatchContexts(DotExpression parent) {
    FailDotExpression fail = new FailDotExpression(location);
    for (int ix = 0; ix < children.length; ix++)
      children[ix].computeMatchContexts(fail);
  }

  public ExpressionNode optimize() {
    boolean allLiterals = true;
    for (int ix = 0; ix < children.length; ix++) {
      children[ix] = children[ix].optimize();
      allLiterals = allLiterals && (children[ix] instanceof LiteralExpression);
    }
    if (!allLiterals)
      return this;

    // we're a static array expression. we can just make the array and
    // turn that into a literal, instead of creating it over and over
    JsonNode array = apply(null, null); // literals won't use scope or input
    return new LiteralExpression(array, location);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + '[');
    for (int ix = 0; ix < children.length; ix++)
      children[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ']');
  }
}
