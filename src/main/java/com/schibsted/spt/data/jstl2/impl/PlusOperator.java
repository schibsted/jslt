
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

public class PlusOperator implements ExpressionNode {
  private ExpressionNode left;
  private ExpressionNode right;

  public PlusOperator(ExpressionNode left, ExpressionNode right) {
    this.left = left;
    this.right = right;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode v1 = left.apply(scope, input);
    JsonNode v2 = right.apply(scope, input);

    return new TextNode(NodeUtils.toString(v1, false) +
                        NodeUtils.toString(v2, false));
  }

  public void dump(int level) {
    left.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + "+");
    right.dump(level + 1);
  }
}
