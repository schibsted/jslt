
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.Expression;

public class LiteralExpression implements Expression {
  private JsonNode value;

  public LiteralExpression(JsonNode value) {
    this.value = value;
  }

  public JsonNode apply(JsonNode input) {
    return value;
  }

}
