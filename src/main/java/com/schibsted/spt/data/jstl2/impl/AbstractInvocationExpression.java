
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

/**
 * Common superclass for function and macro expressions, to avoid
 * having to repeat so much code.
 */
public abstract class AbstractInvocationExpression extends AbstractNode {
  private Callable callable; // null until resolve is called
  protected ExpressionNode[] arguments;

  public AbstractInvocationExpression(ExpressionNode[] arguments,
                                      Location location) {
    super(location);
    this.arguments = arguments;
  }

  // invoked when we know which callable it's going to be
  public void resolve(Callable callable) {
    this.callable = callable;
    if (arguments.length < callable.getMinArguments() ||
        arguments.length > callable.getMaxArguments()) {
      String kind = (this instanceof FunctionExpression) ? "Function" : "Macro";
      throw new JstlException(
        kind + " '" + callable.getName() + "' needs " +
        callable.getMinArguments() + "-" + callable.getMaxArguments() +
        " arguments, got " + arguments.length, location
      );
    }
  }

  public ExpressionNode optimize() {
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix] = arguments[ix].optimize();
    return this;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + callable.getName() + "(");
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ')');
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(callable.getName());
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
