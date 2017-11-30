
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilities for test cases.
 */
public class TestBase {
  private static ObjectMapper mapper = new ObjectMapper();

  void check(String input, String query, String result) {
    try {
      JsonNode context = mapper.readTree(input);

      Expression expr = Parser.compile(query);
      JsonNode actual = expr.apply(context);

      JsonNode expected = mapper.readTree(result);

      assertEquals(expected, actual, "actual class " + actual.getClass() + ", expected class " + expected.getClass());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // result must be contained in the error message
  void error(String query, String result) {
    try {
      JsonNode context = mapper.readTree("{}");

      Expression expr = Parser.compile(query);
      JsonNode actual = expr.apply(context);
      fail("JSTL did not detect error");
    } catch (JstlException e) {
      assertTrue(e.getMessage().indexOf(result) != -1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
