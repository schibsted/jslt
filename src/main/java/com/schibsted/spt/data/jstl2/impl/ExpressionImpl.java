
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
  private LetExpression[] lets;
  private ExpressionNode actual;

  public ExpressionImpl(LetExpression[] lets, ExpressionNode actual) {
    this.lets = lets;
    this.actual = actual;
  }

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input) {
    Scope scope = NodeUtils.evalLets(Scope.makeScope(variables), input, lets);
    return actual.apply(scope, input);
  }

  public JsonNode apply(JsonNode input) {
    Scope scope = NodeUtils.evalLets(Scope.getRoot(), input, lets);
    return actual.apply(scope, input);
  }

  public void dump() {
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].dump(0);
    actual.dump(0);
  }
}
