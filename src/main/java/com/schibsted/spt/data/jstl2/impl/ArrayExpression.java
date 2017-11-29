
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jstl2.Expression;

public class ArrayExpression implements Expression {
  private ObjectMapper mapper;
  private Expression[] children;

  public ArrayExpression(ObjectMapper mapper, Expression[] children) {
    this.mapper = mapper;
    this.children = children;
  }

  public JsonNode apply(JsonNode input) {
    ArrayNode array = mapper.createArrayNode();
    for (int ix = 0; ix < children.length; ix++)
      array.add(children[ix].apply(input));
    return array;
  }

}
