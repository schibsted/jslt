
package com.schibsted.spt.data.jslt;

import java.util.Map;
import java.util.List;
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
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jslt.parser.*;
import com.schibsted.spt.data.jslt.impl.*;

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

  private Parser(String source, Reader reader, Collection<Function> functions) {
    this.functions = functions;
    this.source = source;
    this.reader = reader;
  }

  /**
   * Create a Parser reading JSLT source from the given Reader.
   */
  public Parser(Reader reader) {
    this("<unknown>", reader, Collections.EMPTY_SET);
  }

  /**
   * Create a new Parser with the given source name. The name is a string
   * used in error messages.
   */
  public Parser withSource(String thisSource) {
    return new Parser(thisSource, reader, functions);
  }

  /**
   * Create a new Parser with the given extension functions.
   */
  public Parser withFunctions(Collection<Function> theseFunctions) {
    return new Parser(source, reader, theseFunctions);
  }

  /**
   * Compile the JSLT from the defined parameters.
   */
  public Expression compile() {
    ParseContext ctx = new ParseContext(functions, source);
    return ParserImpl.compileExpression(ctx, new JsltParser(reader));
  }
}
