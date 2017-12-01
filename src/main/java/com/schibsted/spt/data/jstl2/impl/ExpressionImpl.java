
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Expression;

/**
 * Wrapper class that translates an external Expression to an
 * ExpressionNode.
 */
public class ExpressionImpl implements Expression {
  private ExpressionNode actual;

  public ExpressionImpl(ExpressionNode actual) {
    this.actual = actual;
  }

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input) {
    return actual.apply(Scope.makeScope(variables), input);
  }

  public JsonNode apply(JsonNode input) {
    return actual.apply(Scope.getRoot(), input);
  }

}
