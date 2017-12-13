
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

// not sure we actually need this ExpressionNode class. maybe macros
// should be expressions in their own right? it does mean we get to
// do the parameter count checking just once, though. we also need to
// see whether macros are going to be an external or internal feature.

public class MacroExpression extends AbstractNode {
  private Macro macro;
  private ExpressionNode[] arguments;

  public MacroExpression(Macro macro, ExpressionNode[] arguments,
                         Location location) {
    super(location);
    this.macro = macro;
    this.arguments = arguments;

    if (arguments.length < macro.getMinArguments() ||
        arguments.length > macro.getMaxArguments())
      throw new JstlException(
        "Function '" + macro.getName() + "' needs " +
        macro.getMinArguments() + "-" + macro.getMaxArguments() +
        " arguments, got " + arguments.length, location
      );
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return macro.call(scope, input, arguments);
  }

  public ExpressionNode optimize() {
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix] = arguments[ix].optimize();
    return this;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + macro.getName() + "(");
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ')');
  }
}
