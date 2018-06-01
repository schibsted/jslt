
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

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
  private Collection<FunctionExpression> funcalls; // delayed function resolution

  public ParseContext(Collection<Function> extensions, String source) {
    this.functions = new HashMap();
    for (Function func : extensions)
      functions.put(func.getName(), func);

    this.source = source;
    this.funcalls = new ArrayList();
  }

  public ParseContext(String source) {
    this.functions = new HashMap();
    this.source = source;
    this.funcalls = new ArrayList();
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

  public void addDeclaredFunction(String name, Function function) {
    functions.put(name, function);
  }

  public void rememberFunctionCall(FunctionExpression fun) {
    funcalls.add(fun);
  }

  // called at the end to resolve all the functions by name
  public void resolveFunctions() {
    for (FunctionExpression fun : funcalls) {
      String name = fun.getFunctionName();
      Function f = getFunction(name);
      if (f == null)
        throw new JstlException("No such function: '" + name + "'",
                                fun.getLocation());
      fun.resolve(f);
    }
  }
}
