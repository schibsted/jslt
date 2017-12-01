
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ArrayExpression implements ExpressionNode {
  private ObjectMapper mapper;
  private ExpressionNode[] children;

  public ArrayExpression(ObjectMapper mapper, ExpressionNode[] children) {
    this.mapper = mapper;
    this.children = children;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    ArrayNode array = mapper.createArrayNode();
    for (int ix = 0; ix < children.length; ix++)
      array.add(children[ix].apply(scope, input));
    return array;
  }
}
