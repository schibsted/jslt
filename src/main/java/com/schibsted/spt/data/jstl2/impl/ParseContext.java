
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import com.schibsted.spt.data.jstl2.Function;

/**
 * Class to encapsulate context information like available functions,
 * parser/compiler settings, and so on, during parsing.
 */
public class ParseContext {
  private Map<String, Function> functions;
  /**
   * What file/resource are we parsing? Can be null, in cases where we
   * don't have this information.
   */
  private String source;

  public ParseContext(Collection<Function> extensions, String source) {
    this.functions = new HashMap();
    for (Function func : extensions)
      functions.put(func.getName(), func);

    this.source = source;
  }

  public ParseContext(String source) {
    this.functions = Collections.EMPTY_MAP;
    this.source = source;
  }

  public Function getFunction(String name) {
    Function func = functions.get(name);
    if (func == null)
      func = BuiltinFunctions.functions.get(name);
    return func;
  }

  public Macro getMacro(String name) {
    return BuiltinFunctions.macros.get(name);
  }

  public String getSource() {
    return source;
  }
}
