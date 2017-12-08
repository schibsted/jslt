
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.DoubleNode;

public class PlusOperator extends AbstractOperator {

  public PlusOperator(ExpressionNode left, ExpressionNode right) {
    super(left, right, "+");
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    if (v1.isTextual() || v2.isTextual()) {
      // if one operand is string: do string concatenation
      return new TextNode(NodeUtils.toString(v1, false) +
                          NodeUtils.toString(v2, false));
    } else {
      v1 = NodeUtils.number(v1);
      v2 = NodeUtils.number(v2);

      if (v1.isNull() || v2.isNull())
        return NullNode.instance;
      else if (v1.isIntegralNumber() && v2.isIntegralNumber())
        return new LongNode(v1.longValue() + v2.longValue());
      else
        return new DoubleNode(v1.doubleValue() + v2.doubleValue());
    }
  }
}
