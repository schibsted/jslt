
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class FunctionExpression extends AbstractInvocationExpression {
  private Function function;

  public FunctionExpression(Function function, ExpressionNode[] arguments,
                            Location location) {
    super(function, arguments, location);
    this.function = function;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode[] params = new JsonNode[arguments.length];
    for (int ix = 0; ix < params.length; ix++)
      params[ix] = arguments[ix].apply(scope, input);

    return function.call(input, params);
  }

  public void compile(Compiler compiler) {
    // have to do this backwards, so that arguments wind up in 0, 1, 2, ...
    // order on the stack
    for (int ix = arguments.length - 1; ix >= 0; ix--)
      arguments[ix].compile(compiler);
    compiler.genPUSHL(new IntNode(arguments.length));
    compiler.genCALL(function);
  }
}
