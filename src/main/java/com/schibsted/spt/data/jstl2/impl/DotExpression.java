
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.schibsted.spt.data.jstl2.Expression;

public class DotExpression implements Expression {
  private String key;

  public DotExpression() {
  }

  public DotExpression(String key) {
    this.key = key;
  }

  public JsonNode apply(JsonNode input) {
    if (key == null)
      return input; // FIXME: should we make a copy?

    JsonNode value = input.get(key);
    if (value == null)
      value = NullNode.instance;
    return value;
  }

}
