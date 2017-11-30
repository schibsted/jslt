
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.Expression;
import com.schibsted.spt.data.jstl2.JstlException;

public class FunctionExpression implements Expression {
  private Function function;
  private Expression[] arguments;

  public FunctionExpression(Function function, Expression[] arguments) {
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

  public JsonNode apply(JsonNode input) {
    return function.call(input, arguments);
  }

}
