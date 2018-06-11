
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.schibsted.spt.data.jslt.JsltException;

public class LiteralExpression extends AbstractNode {
  private JsonNode value;

  public LiteralExpression(JsonNode value, Location location) {
    super(location);
    this.value = value;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + value);
  }

  public String toString() {
    try {
      return NodeUtils.mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new JsltException("Couldn't serialize literal value: " + e);
    }
  }
}
