
// Copyright 2018 Schibsted Marketplaces Products & Technology As
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.schibsted.spt.data.jslt.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.schibsted.spt.data.jslt.Module;
import com.schibsted.spt.data.jslt.Callable;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.ResourceResolver;

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
  private ResourceResolver resolver;
  /**
   * Named modules listed under their identifiers.
   */
  private Map<String, Module> namedModules;

  public ParseContext(Collection<Function> extensions, String source,
                      ResourceResolver resolver,
                      Map<String, Module> namedModules) {
    this.extensions = extensions;
    this.functions = new HashMap();
    for (Function func : extensions)
      functions.put(func.getName(), func);

    this.source = source;
    this.funcalls = new ArrayList();
    this.modules = new HashMap();
    this.resolver = resolver;
    this.namedModules = namedModules;

    namedModules.put(ExperimentalModule.URI, new ExperimentalModule());
  }

  public ParseContext(String source) {
    this(Collections.EMPTY_SET, source, new ClasspathResourceResolver(),
         new HashMap());
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

  public Module getNamedModule(String identifier) {
    return namedModules.get(identifier);
  }

  public Map<String, Module> getNamedModules() {
    return namedModules;
  }

  public boolean isAlreadyImported(String module) {
    if (source != null && module.equals(source))
      return true;
    if (parent != null)
      return parent.isAlreadyImported(module);
    return false;
  }

  public Callable getImportedCallable(String prefix, String name, Location loc) {
    Module m = modules.get(prefix);
    if (m == null)
      throw new JsltException("No such module '" + prefix + "'", loc);

    Callable f = m.getCallable(name);
    if (f == null)
      throw new JsltException("No such function '" + name+ "' in module '" + prefix + "'", loc);

    return f;
  }

  public ResourceResolver getResolver() {
    return resolver;
  }
}
