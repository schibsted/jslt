
package com.schibsted.spt.data.jslt.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.Function;

/**
 * Class to encapsulate context information like available functions,
 * parser/compiler settings, and so on, during parsing.
 */
public class ParseContext {
  private Collection<Function> extensions;
  private Map<String, Function> functions;
  /**
   * What file/resource are we parsing? Can be null, in cases where we
   * don't have this information.
   */
  private String source;
  /**
   * Imported modules listed under their prefixes.
   */
  private Map<String, Module> modules;
  private Collection<FunctionExpression> funcalls; // delayed function resolution
  private ParseContext parent;

  public ParseContext(Collection<Function> extensions, String source) {
    this.extensions = extensions;
    this.functions = new HashMap();
    for (Function func : extensions)
      functions.put(func.getName(), func);

    this.source = source;
    this.funcalls = new ArrayList();
    this.modules = new HashMap();
  }

  public ParseContext(String source) {
    this(Collections.EMPTY_SET, source);
  }

  public void setParent(ParseContext parent) {
    this.parent = parent;
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

  public Collection<Function> getExtensions() {
    return extensions;
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
        throw new JsltException("No such function: '" + name + "'",
                                fun.getLocation());
      fun.resolve(f);
    }
  }

  public Map<String, Function> getDeclaredFunctions() {
    return functions;
  }

  public void registerModule(String prefix, Module module) {
    modules.put(prefix, module);
  }

  public boolean isAlreadyImported(String module) {
    if (source != null && module.equals(source))
      return true;
    if (parent != null)
      return parent.isAlreadyImported(module);
    return false;
  }

  public Function getImportedFunction(String prefix, String name, Location loc) {
    Module m = modules.get(prefix);
    if (m == null)
      throw new JsltException("No such module '" + prefix + "'", loc);

    Function f = m.getFunction(name);
    if (f == null)
      throw new JsltException("No such function '" + name+ "' in module '" + prefix + "'", loc);

    return f;
  }
}
