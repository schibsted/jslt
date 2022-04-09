
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

package com.schibsted.spt.data.jslt;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import com.schibsted.spt.data.jslt.parser.*;
import com.schibsted.spt.data.jslt.impl.*;
import com.schibsted.spt.data.jslt.filters.*;

/**
 * Parses JSLT expressions to Expression objects for evaluating them.
 */
public class Parser {

  /**
   * Compile the given JSLT file.
   */
  public static Expression compile(File jslt) {
    return compile(jslt, Collections.EMPTY_SET);
  }

  /**
   * Compile the given JSLT file with the given predefined functions.
   */
  public static Expression compile(File jslt, Collection<Function> functions) {
    try (FileReader f = new FileReader(jslt)) {
      return new Parser(f)
        .withSource(jslt.getAbsolutePath())
        .withFunctions(functions)
        .compile();
    } catch (FileNotFoundException e) {
      throw new JsltException("Couldn't find file " + jslt);
    } catch (IOException e) {
      throw new JsltException("Couldn't read file " + jslt, e);
    }
  }

  /**
   * Compile JSLT expression given as an inline string.
   */
  public static Expression compileString(String jslt) {
    return compileString(jslt, Collections.EMPTY_SET);
  }

  /**
   * Compile JSLT expression given as an inline string with the given
   * extension functions.
   */
  public static Expression compileString(String jslt,
                                         Collection<Function> functions) {
    return new Parser(new StringReader(jslt))
      .withSource("<inline>")
      .withFunctions(functions)
      .compile();
  }

  /**
   * Load and compile JSLT expression from the classpath.
   */
  public static Expression compileResource(String jslt) {
    return compileResource(jslt, Collections.EMPTY_SET);
  }

  /**
   * Load and compile JSLT expression from the classpath with the
   * given extension functions.
   */
  public static Expression compileResource(String jslt,
                                           Collection<Function> functions) {
    try (InputStream stream = Parser.class.getClassLoader().getResourceAsStream(jslt)) {
      if (stream == null)
        throw new JsltException("Cannot load resource '" + jslt + "': not found");

      Reader reader = new InputStreamReader(stream, "UTF-8");
      return new Parser(reader)
        .withSource(jslt)
        .withFunctions(functions)
        .compile();
    } catch (IOException e) {
      throw new JsltException("Couldn't read resource " + jslt, e);
    }
  }

  /**
   * Compile JSLT expression from the Reader. The source is just a
   * name used in error messages, and has no practical effect.
   */
  public static Expression compile(String source,
                                   Reader reader,
                                   Collection<Function> functions) {
    return new Parser(reader)
      .withSource(source)
      .withFunctions(functions)
      .compile();
  }

  // ===== FLUENT BUILDER API

  private Collection<Function> functions;
  private String source;
  private Reader reader;
  private ResourceResolver resolver;
  private Map<String, Module> modules;
  private JsonFilter objectFilter;

  private Parser(String source, Reader reader, Collection<Function> functions,
                 ResourceResolver resolver, Map<String, Module> modules,
                 JsonFilter filter) {
    this.functions = functions;
    this.source = source;
    this.reader = reader;
    this.resolver = resolver;
    this.modules = modules;
    this.objectFilter = filter;
  }

  /**
   * Create a Parser reading JSLT source from the given Reader. Uses a
   * {@link ClasspathResourceResolver} for import statements.
   */
  public Parser(Reader reader) {
    this("<unknown>", reader, Collections.EMPTY_SET,
         new ClasspathResourceResolver(), new HashMap(),
         new DefaultJsonFilter());
  }

  /**
   * Create a new Parser with the given source name. The name is a string
   * used in error messages.
   */
  public Parser withSource(String thisSource) {
    return new Parser(thisSource, reader, functions, resolver, modules,
                      objectFilter);
  }

  /**
   * Create a new Parser with the given extension functions.
   */
  public Parser withFunctions(Collection<Function> theseFunctions) {
    return new Parser(source, reader, theseFunctions, resolver, modules,
                      objectFilter);
  }

  /**
   * Create a new Parser with the given resource resolver.
   */
  public Parser withResourceResolver(ResourceResolver thisResolver) {
    return new Parser(source, reader, functions, thisResolver, modules,
                      objectFilter);
  }

  /**
   * Create a new Parser with the given modules registered. The keys
   * in the map are the module "names", and importing these names will
   * bind a prefix to the modules in this map. The names can follow
   * any syntax.
   */
  public Parser withNamedModules(Map<String, Module> thisModules) {
    return new Parser(source, reader, functions, resolver, thisModules,
                      objectFilter);
  }

  /**
   * Create a new Parser with the given filter for object values. For
   * all key/value pairs in objects being created, if this filter
   * returns false when given the value, the key/value pair is
   * omitted.
   */
  public Parser withObjectFilter(String filter) {
    Expression parsedFilter = Parser.compileString(filter);
    return new Parser(source, reader, functions, resolver, modules,
                      new JsltJsonFilter(parsedFilter));
  }

  /**
   * Create a new Parser with the given filter for object values. For
   * all key/value pairs in objects being created, if this filter
   * returns false when given the value, the key/value pair is
   * omitted.
   */
  public Parser withObjectFilter(JsonFilter filter) {
    return new Parser(source, reader, functions, resolver, modules,
                      filter);
  }

  /**
   * Compile the JSLT from the defined parameters.
   */
  public Expression compile() {
    ParseContext ctx = new ParseContext(functions, source, resolver, modules,
                                        new ArrayList(),
                                        new PreparationContext(),
                                        objectFilter);
    return ParserImpl.compileExpression(ctx, new JsltParser(reader));
  }
}
