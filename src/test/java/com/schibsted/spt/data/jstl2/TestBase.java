
package com.schibsted.spt.data.jstl2;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilities for test cases.
 */
public class TestBase {
  static ObjectMapper mapper = new ObjectMapper();

  Map<String, JsonNode> makeVars(String var, String val) {
    try {
      Map<String, JsonNode> map = new HashMap();
      map.put(var, mapper.readTree(val));
      return map;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void check(String input, String query, String result) {
    check(input, query, result, Collections.EMPTY_MAP, Collections.EMPTY_SET);
  }

  void check(String input, String query, String result,
             Map<String, JsonNode> variables) {
    check(input, query, result, variables, Collections.EMPTY_SET);
  }

  void check(String input, String query, String result,
             Map<String, JsonNode> variables,
             Collection<Function> functions) {
    try {
      JsonNode context = mapper.readTree(input);

      Expression expr = Parser.compile(functions, query);
      JsonNode actual = expr.apply(variables, context);

      // reparse to handle IntNode(2) != LongNode(2)
      actual = mapper.readTree(mapper.writeValueAsString(actual));

      JsonNode expected = mapper.readTree(result);

      assertEquals("actual class " + actual.getClass() + ", expected class " + expected.getClass(), expected, actual);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  JsonNode execute(String input, String query) {
    try {
      JsonNode context = mapper.readTree(input);
      Expression expr = Parser.compile(query);
      return expr.apply(context);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // result must be contained in the error message
  void error(String query, String result) {
    error("{}", query, result);
  }

  // result must be contained in the error message
  void error(String input, String query, String result) {
    try {
      JsonNode context = mapper.readTree(input);

      Expression expr = Parser.compile(query);
      JsonNode actual = expr.apply(context);
      fail("JSTL did not detect error");
    } catch (JstlException e) {
      assertTrue("incorrect error message: '" + e.getMessage() + "'",
                 e.getMessage().indexOf(result) != -1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
