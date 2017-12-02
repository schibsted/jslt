
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class LiteralExpression implements ExpressionNode {
  private JsonNode value;

  public LiteralExpression(JsonNode value) {
    this.value = value;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + value);
  }
}
