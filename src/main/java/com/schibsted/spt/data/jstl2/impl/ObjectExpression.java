
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectExpression implements ExpressionNode {
  private LetExpression[] lets;
  private PairExpression[] children;

  public ObjectExpression(LetExpression[] lets,
                          PairExpression[] children) {
    this.lets = lets;
    this.children = children;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    scope = NodeUtils.evalLets(scope, input, lets);

    ObjectNode object = NodeUtils.mapper.createObjectNode();
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
