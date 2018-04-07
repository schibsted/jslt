
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface implemented by all macros. A macro is like a function,
 * except that it controls the evaluation of its arguments itself.
 * That allows it to do things that an ordinary function cannot do.
 * Macros are an internal feature for now.
 */
public interface Macro extends Callable {

  /**
   * Invokes the macro, which can then modify the input node and
   * evaluate the parameters as needed.
   */
  public JsonNode call(Scope scope, JsonNode input, ExpressionNode[] parameters);

}
