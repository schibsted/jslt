
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jslt.Expression;

/**
 * Wrapper class that translates an external Expression to an
 * ExpressionNode.
 */
public class ExpressionImpl implements Expression {
  private LetExpression[] lets;
  private Map<String, Function> functions;
  private ExpressionNode actual;

  public ExpressionImpl(LetExpression[] lets, Map<String, Function> functions,
                        ExpressionNode actual) {
    this.lets = lets;
    this.functions = functions;
    this.actual = actual;

    // traverse tree and set up context queries
    if (actual != null)
      actual.computeMatchContexts(new DotExpression(null));
  }

  public Function getFunction(String name) {
    return functions.get(name);
  }

  public boolean hasBody() {
    return actual != null;
  }

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input) {
    // Jackson 2.9.2 can parse to Java null. See unit test
    // QueryTest.testNullInput. so we have to handle that
    if (input == null)
      input = NullNode.instance;

    Scope scope = NodeUtils.evalLets(Scope.makeScope(variables), input, lets);
    return actual.apply(scope, input);
  }

  public JsonNode apply(JsonNode input) {
    // Jackson 2.9.2 can parse to Java null. See unit test
    // QueryTest.testNullInput. so we have to handle that
    if (input == null)
      input = NullNode.instance;

    Scope scope = NodeUtils.evalLets(Scope.getRoot(), input, lets);
    return actual.apply(scope, input);
  }

  public void dump() {
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].dump(0);
    actual.dump(0);
  }

  public String toString() {
    // FIXME: letexprs
    return actual.toString();
  }
}
