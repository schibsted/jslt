
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

import java.net.URISyntaxException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.ResourceResolver;

import static com.schibsted.spt.data.jslt.impl.JSLTFunctions.iteratorToStream;

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
  final BuiltinFunctions builtinFunctions;

  public ParseContext(Collection<Function> extensions, String source,
                      ResourceResolver resolver) {
    this.extensions = extensions;
    this.functions = new HashMap();
    for (Function func : extensions)
      functions.put(func.getName(), func);

    this.source = source;
    this.funcalls = new ArrayList();
    this.modules = new HashMap();
    this.resolver = resolver;
    this.builtinFunctions = new BuiltinFunctions();
    init();
  }

  private void init(){
    final ServiceLoader<JSLTFunctions> jsltFunctions =
            ServiceLoader.<JSLTFunctions>load(JSLTFunctions.class);

    if (jsltFunctions != null) {
        final Map<String, JSLTFunctions> moduleMap = iteratorToStream(jsltFunctions.iterator(), true).parallel()
                .filter(distinctByKey(function -> getPrefix(function)))
                .collect(Collectors.toMap(function -> getPrefix(function),function -> function));
        modules.putAll(moduleMap);
    }
  }

  public static <T> Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  private String getPrefix(JSLTFunctions function) {
    try {
      return function.getNamespace().getPrefix();
    } catch (URISyntaxException use) {
      throw new RuntimeException(use.getMessage());
    }
  }

  public ParseContext(String source) {
    this(Collections.EMPTY_SET, source, new ClasspathResourceResolver());
  }

  public void setParent(ParseContext parent) {
    this.parent = parent;
  }

  public Function getFunction(String name) {
    Function func = functions.get(name);
    if (func == null) {
      func = builtinFunctions.getFunction(name);
    }
    return func;
  }

  public Macro getMacro(String name) {
    return builtinFunctions.macros().get(name);
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

  public ResourceResolver getResolver() {
    return resolver;
  }
}
