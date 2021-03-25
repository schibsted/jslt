
package com.schibsted.spt.data.jslt;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.schibsted.spt.data.json.*;

/**
 * Utilities for test cases.
 */
public class TestBase {

  Map<String, JsonValue> makeVars(String var, String val) {
    Map<String, JsonValue> map = new HashMap();
    map.put(var, JsonIO.parseString(val));
    return map;
  }

  void check(String input, String query, String result) {
    check(input, query, result, Collections.EMPTY_MAP, Collections.EMPTY_SET);
  }

  void check(String input, String query, String result,
             Map<String, JsonValue> variables) {
    check(input, query, result, variables, Collections.EMPTY_SET);
  }

  void check(String input, String query, String result,
             Map<String, JsonValue> variables,
             Collection<Function> functions) {
    JsonValue context = JsonIO.parseString(input);

    Expression expr = Parser.compileString(query, functions);
    JsonValue actual = expr.apply(variables, context);
    if (actual == null)
      throw new JsltException("Returned Java null");

    JsonValue expected = JsonIO.parseString(result);

    assertEquals("actual class " + actual.getClass() + ", expected class " + expected.getClass(), expected, actual);
  }

  String load(String resource) {
    try (InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream(resource)) {
      if (stream == null)
        throw new JsltException("Cannot load resource '" + resource + "': not found");

      char[] tmp = new char[128];
      Reader reader = new InputStreamReader(stream, "UTF-8");
      StringBuilder buf = new StringBuilder();
      while (true) {
        int chars = reader.read(tmp, 0, tmp.length);
        if (chars == -1)
          break;
        buf.append(tmp, 0, chars);
      }
      return buf.toString();
    } catch (IOException e) {
      throw new JsltException("Couldn't read resource " + resource, e);
    }
  }

  JsonValue execute(String input, String query) {
    JsonValue context = JsonIO.parseString(input);
    Expression expr = Parser.compileString(query);
    return expr.apply(context);
  }

  // result must be contained in the error message
  void error(String query, String result) {
    error("{}", query, result);
  }

  // result must be contained in the error message
  void error(String input, String query, String result) {
    try {
      JsonValue context = JsonIO.parseString(input);

      Expression expr = Parser.compileString(query);
      JsonValue actual = expr.apply(context);
      fail("JSLT did not detect error");
    } catch (JsltException e) {
      assertTrue("incorrect error message: '" + e.getMessage() + "'",
                 e.getMessage().indexOf(result) != -1);
    }
  }

}
