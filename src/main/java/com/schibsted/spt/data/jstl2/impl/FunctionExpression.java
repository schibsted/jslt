
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class FunctionExpression extends AbstractNode {
  private Function function;
  private ExpressionNode[] arguments;

  public FunctionExpression(Function function, ExpressionNode[] arguments,
                            Location location) {
    super(location);
    this.function = function;
    this.arguments = arguments;

    if (arguments.length < function.getMinArguments() ||
        arguments.length > function.getMaxArguments())
      throw new JstlException(
        "Function '" + function.getName() + "' needs " +
        function.getMinArguments() + "-" + function.getMaxArguments() +
        " arguments, got " + arguments.length, location
      );
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

  public ExpressionNode optimize() {
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix] = arguments[ix].optimize();
    return this;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + function.getName() + "(");
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ')');
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(function.getName());
    buf.append('(');
    for (int ix = 0; ix < arguments.length; ix++) {
      if (ix > 0)
        buf.append(", ");
      buf.append(arguments[ix].toString());
    }
    buf.append(')');

    return buf.toString();
  }
}
