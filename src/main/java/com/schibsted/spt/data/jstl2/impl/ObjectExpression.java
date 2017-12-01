
package com.schibsted.spt.data.jstl2.impl;

import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectExpression implements ExpressionNode {
  private ObjectMapper mapper;
  private LetExpression[] lets;
  private PairExpression[] children;

  public ObjectExpression(ObjectMapper mapper,
                          LetExpression[] lets,
                          PairExpression[] children) {
    this.mapper = mapper;
    this.lets = lets;
    this.children = children;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    for (int ix = 0; ix < lets.length; ix++) {
      String var = lets[ix].getVariable();
      JsonNode val = lets[ix].apply(scope, input);
      scope = Scope.makeScope(Collections.singletonMap(var, val), scope);
    }

    ObjectNode object = mapper.createObjectNode();
    for (int ix = 0; ix < children.length; ix++) {
      JsonNode value = children[ix].apply(scope, input);
      if (isValue(value))
        object.put(children[ix].getKey(), value);
    }
    return object;
  }

  private boolean isValue(JsonNode value) {
    return !value.isNull() &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isArray() && value.size() == 0);
  }
}
