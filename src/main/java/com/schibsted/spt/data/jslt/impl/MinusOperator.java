
package com.schibsted.spt.data.jslt.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jslt.JsltException;

public class MinusOperator extends NumericOperator {

  public MinusOperator(ExpressionNode left, ExpressionNode right,
                       Location location) {
    super(left, right, "-", location);
  }

  // we only support the numeric operation and nothing else

  protected double perform(double v1, double v2) {
    return v1 - v2;
  }

  protected long perform(long v1, long v2) {
    return v1 - v2;
  }
}
