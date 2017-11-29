
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jstl2.Expression;

public class ObjectExpression implements Expression {
  private ObjectMapper mapper;
  private PairExpression[] children;

  public ObjectExpression(ObjectMapper mapper, PairExpression[] children) {
    this.mapper = mapper;
    this.children = children;
  }

  public JsonNode apply(JsonNode input) {
    ObjectNode object = mapper.createObjectNode();
    for (int ix = 0; ix < children.length; ix++)
      object.put(children[ix].getKey(), children[ix].apply(input));
    return object;
  }

}
