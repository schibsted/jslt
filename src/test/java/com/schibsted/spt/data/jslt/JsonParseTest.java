
package com.schibsted.spt.data.jslt;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON parsing test cases. Verifies that Jackson and JSLT produce the
 * same JSON structure.
 */
@RunWith(Parameterized.class)
public class JsonParseTest {
  private static ObjectMapper mapper = new ObjectMapper();
  private String json;

  public JsonParseTest(String json) {
    this.json = json;
  }

  @Test
  public void check() {
    try {
      Expression expr = Parser.compileString(json);
      JsonNode actual = expr.apply(null);

      JsonNode expected = mapper.readTree(json);

      assertEquals("actual class " + actual.getClass() + ", expected class " + expected.getClass(), expected, actual);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // @Test
  // public void testUndefinedEscape() {
  //   error("\" \\d \"");
  // }

  // @Test
  // public void testUnfinishedUnicodeEscape1() {
  //   error("\"\\u\"");
  // }

  // @Test
  // public void testUnfinishedUnicodeEscape2() {
  //   error("\"\\u0\"");
  // }

  // @Test
  // public void testUnfinishedUnicodeEscape3() {
  //   error("\"\\u00\"");
  // }

  // @Test
  // public void testUnfinishedUnicodeEscape4() {
  //   error("\"\\u004\"");
  // }

  // @Test
  // public void testInitialZeroInNumber() {
  //   error("0123");
  // }

  // @Test
  // public void testInitialZeroInNegativeNumber() {
  //   error("-0123");
  // }

  // @Test
  // public void testInitialZeroInFloat() {
  //   error("0123.0");
  // }

  // @Test
  // public void testInitialZeroInNegativeFloat() {
  //   error("-0123.0");
  // }

  // @Test
  // public void testObjectKeyOrder() {
  //   Expression expr = Parser.compileString("{\"a\":1, \"b\":2}");
  //   JsonNode actual = expr.apply(null);

  //   Iterator<String> it = actual.fieldNames();
  //   assertEquals("a", it.next());
  //   assertEquals("b", it.next());
  // }

  // private void error(String json) {
  //   try {
  //     Parser.compileString(json);
  //     fail("Successfully parsed " + json);
  //   } catch (JsltException e) {
  //     // this is what we want
  //   }
  //  }

  @Parameters
  public static Collection<Object[]> data() {
    JsonNode json = TestUtils.loadJson("json-parse-tests.json");
    JsonNode tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++)
      strings.add(new Object[] { tests.get(ix).asText() });
    return strings;
  }
}
