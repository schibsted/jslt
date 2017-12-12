
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.DoubleNode;

public abstract class NumericOperator extends AbstractOperator {

  public NumericOperator(ExpressionNode left, ExpressionNode right, String name) {
    super(left, right, name);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    if (v1.isNull() || v2.isNull())
      return NullNode.instance;

    v1 = NodeUtils.number(v1, true);
    v2 = NodeUtils.number(v2, true);

    if (v1.isIntegralNumber() && v2.isIntegralNumber())
      return new LongNode(perform(v1.longValue(), v2.longValue()));
    else
      return new DoubleNode(perform(v1.doubleValue(), v2.doubleValue()));
  }

  protected abstract double perform(double v1, double v2);

  protected abstract long perform(long v1, long v2);
}
