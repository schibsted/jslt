
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jstl2.JstlException;

// not sure we actually need this ExpressionNode class. maybe macros
// should be expressions in their own right? it does mean we get to
// do the parameter count checking just once, though. we also need to
// see whether macros are going to be an external or internal feature.

public class MacroExpression extends AbstractInvocationExpression {
  private Macro macro;

  public MacroExpression(Macro macro, ExpressionNode[] arguments,
                         Location location) {
    super(arguments, location);
    resolve(macro);
    this.macro = macro;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return macro.call(scope, input, arguments);
  }
}
