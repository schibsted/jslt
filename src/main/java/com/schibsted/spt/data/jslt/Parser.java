
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
import com.schibsted.spt.data.jstl2.impl.*;
import com.schibsted.spt.data.jslt.parser.*;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

public class Parser {

  public static Expression compile(Collection<Function> functions,
                                   File jstl) {
    return compile(new ParseContext(functions, jstl.getAbsolutePath()), jstl);
  }

  public static Expression compile(File jstl) {
    return compile(new ParseContext(jstl.getAbsolutePath()), jstl);
  }

  private static Expression compile(ParseContext ctx, File jstl) {
    try (FileReader f = new FileReader(jstl)) {
      return ParserImpl.compileExpression(ctx, new JsltParser(f));
    } catch (FileNotFoundException e) {
      throw new JstlException("Couldn't find file " + jstl);
    } catch (IOException e) {
      throw new JstlException("Couldn't read file " + jstl, e);
    }
  }

  public static Expression compile(String jstl) {
    ParseContext ctx = new ParseContext("<unknown>");
    return ParserImpl.compileExpression(ctx, new JsltParser(new StringReader(jstl)));
  }

  public static Expression compile(Collection<Function> functions,
                                   String jstl) {
    ParseContext ctx = new ParseContext(functions, null);
    return ParserImpl.compileExpression(ctx, new JsltParser(new StringReader(jstl)));
  }

  public static Expression compileResource(String jstl) {
    return compileResource(Collections.EMPTY_SET, jstl);
  }

  /**
   * Compile JSLT expression from the Reader. The resourceName is just
   * a name used in error messages, and has no practical effect.
   */
  public static Expression compile(Collection<Function> functions,
                                   String resourceName,
                                   Reader reader) {
    ParseContext ctx = new ParseContext(functions, resourceName);
    return ParserImpl.compileExpression(ctx, new JsltParser(reader));
  }

  public static Expression compileResource(Collection<Function> functions,
                                           String jslt) {
    try (InputStream stream = Parser.class.getClassLoader().getResourceAsStream(jslt)) {
      if (stream == null)
        throw new JstlException("Cannot load resource '" + jslt + "': not found");

      Reader reader = new InputStreamReader(stream, "UTF-8");
      ParseContext ctx = new ParseContext(functions, jslt);
      return ParserImpl.compileExpression(ctx, new JsltParser(reader));
    } catch (IOException e) {
      throw new JstlException("Couldn't read resource " + jslt, e);
    }
  }
}
