
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

public class DotExpression implements ExpressionNode {
  private String key;

  public DotExpression() {
  }

  public DotExpression(String key) {
    this.key = key;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    if (key == null)
      return input; // FIXME: should we make a copy?

    JsonNode value = input.get(key);
    if (value == null)
      value = NullNode.instance;
    return value;
  }

}
