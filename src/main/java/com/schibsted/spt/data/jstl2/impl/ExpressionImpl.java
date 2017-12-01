
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.Collections;
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
  private LetExpression[] lets;
  private ExpressionNode actual;

  public ExpressionImpl(LetExpression[] lets, ExpressionNode actual) {
    this.lets = lets;
    this.actual = actual;
  }

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input) {
    Scope scope = buildScope(Scope.makeScope(variables), input);
    return actual.apply(scope, input);
  }

  public JsonNode apply(JsonNode input) {
    Scope scope = buildScope(Scope.getRoot(), input);
    return actual.apply(scope, input);
  }

  private Scope buildScope(Scope scope, JsonNode input) {
    for (int ix = 0; ix < lets.length; ix++) {
      String var = lets[ix].getVariable();
      JsonNode val = lets[ix].apply(scope, input);
      scope = Scope.makeScope(Collections.singletonMap(var, val), scope);
    }
    return scope;
  }
}
