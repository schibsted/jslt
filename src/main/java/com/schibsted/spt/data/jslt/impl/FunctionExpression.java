
package com.schibsted.spt.data.jslt.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;

public class FunctionExpression extends AbstractInvocationExpression {
  private Function function; // null before resolution
  private String name;

  public FunctionExpression(String name, ExpressionNode[] arguments,
                            Location location) {
    super(arguments, location);
    this.name = name;
  }

  public String getFunctionName() {
    return name;
  }

  public void resolve(Function function) {
    super.resolve(function);
    this.function = function;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode[] params = new JsonNode[arguments.length];
    for (int ix = 0; ix < params.length; ix++)
      params[ix] = arguments[ix].apply(scope, input);

    return function.call(input, params);
  }
}
