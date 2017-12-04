
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class PlusOperator extends AbstractOperator {

  public PlusOperator(ExpressionNode left, ExpressionNode right) {
    super(left, right, "+");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    return new TextNode(NodeUtils.toString(v1, false) +
                        NodeUtils.toString(v2, false));
  }
}
