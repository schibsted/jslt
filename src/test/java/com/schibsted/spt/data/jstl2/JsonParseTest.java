
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * JSON parsing test cases only. Verifies that Jackson and JSTL
 * produce the same JSON structure.
 */
public class JsonParseTest {
  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testNull() {
    check("null");
  }

  @Test
  public void testTrue() {
    check("true");
  }

  @Test
  public void testFalse() {
    check("false");
  }

  @Test
  public void testInteger() {
    check("1");
  }

  @Test
  public void testBigInteger() {
    check("1321");
  }

  @Test
  public void testNegativeInteger() {
    check("-1321");
  }

  @Test
  public void testDecimal() {
    check("13.21");
  }

  @Test
  public void testNegativeDecimal() {
    check("-13.21");
  }

  @Test
  public void testEmptyString() {
    check("\"\"");
  }

  @Test
  public void testString() {
    check("\"foo\"");
  }

  @Test
  public void testEmptyArray() {
    check("[]");
  }

  @Test
  public void testShortArray() {
    check("[1]");
  }

  @Test
  public void testWhitespace() {
    check(" [ 1 ] ");
  }

  @Test
  public void testLongerArray() {
    check("[1,2,3,4,5]");
  }

  @Test
  public void testNestedArray() {
    check("[[1,2],[3,[4,5]]]");
  }

  @Test
  public void testEmptyObject() {
    check("{}");
  }

  @Test
  public void testNestedSmallObject() {
    check("{\"empty\" : {\"id\" : 1}}");
  }

  @Test
  public void testComplicatedObject() {
    check("{\"foo\" : \"bar\", \"array\" : [7,6,5,4,null], \"koko\":233}");
  }

  private void check(String json) {
    try {
      Expression expr = Parser.compile(json);
      JsonNode actual = expr.apply(null);

      JsonNode expected = mapper.readTree(json);

      assertEquals(expected, actual, "actual class " + actual.getClass() + ", expected class " + expected.getClass());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
