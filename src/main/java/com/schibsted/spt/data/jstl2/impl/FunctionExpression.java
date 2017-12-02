
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

public class FunctionExpression implements ExpressionNode {
  private Function function;
  private ExpressionNode[] arguments;

  public FunctionExpression(Function function, ExpressionNode[] arguments) {
    this.function = function;
    this.arguments = arguments;

    if (arguments.length < function.getMinArguments() ||
        arguments.length > function.getMaxArguments())
      throw new JstlException(
        "Function '" + function.getName() + "' needs " +
        function.getMinArguments() + "-" + function.getMaxArguments() +
        " arguments, got " + arguments.length
      );
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode[] params = new JsonNode[arguments.length];
    for (int ix = 0; ix < params.length; ix++)
      params[ix] = arguments[ix].apply(scope, input);

    return function.call(input, params);
  }

  public void dump(int level) {
  }
}
