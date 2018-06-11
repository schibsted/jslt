
package com.schibsted.spt.data.jslt.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.JsltException;

public class SmallerOrEqualsComparison extends ComparisonOperator {

  public SmallerOrEqualsComparison(ExpressionNode left, ExpressionNode right,
                                 Location location) {
    super(left, right, ">=", location);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    return NodeUtils.toJson(compare(v1, v2) <= 0);
  }

}
