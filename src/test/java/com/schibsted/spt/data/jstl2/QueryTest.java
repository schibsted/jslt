
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Test cases verifying queries against an input.
 */
public class QueryTest {
  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testDot() {
    check("{}", ".", "{}");
  }

  private void check(String input, String query, String result) {
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

}
