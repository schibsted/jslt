
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import com.schibsted.spt.data.jstl2.Function;

/**
 * Class to encapsulate context information like available functions,
 * parser/compiler settings, and so on, during parsing.
 */
public class ParseContext {
  private Map<String, Function> functions;

  public ParseContext(Collection<Function> extensions) {
    this.functions = new HashMap();
    for (Function func : extensions)
      functions.put(func.getName(), func);
  }

  public Function getFunction(String name) {
    Function func = functions.get(name);
    if (func == null)
      func = BuiltinFunctions.functions.get(name);
    return func;
  }
}
