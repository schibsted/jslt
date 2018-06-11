
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;

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

  @Test
  public void testEscapedQuote() {
    check("\" \\\" \""); // \\\" -> \" in the actual string that's parsed
  }

  @Test
  public void testEscapedBackslash() {
    check("\" \\\\ \""); // \\\\ -> \\ in the actual string that's parsed
  }

  @Test
  public void testEscapedNewline() {
    check("\" \\n \""); // \\n -> \n in the actual string that's parsed
  }

  @Test
  public void testEscapedReturn() {
    check("\" \\r \"");
  }

  @Test
  public void testEscapedSlash() {
    check("\" \\/ \"");
  }

  @Test
  public void testEscapedBackspace() {
    check("\" \\b \"");
  }

  @Test
  public void testEscapedTab() {
    check("\" \\t \"");
  }

  @Test
  public void testEscapedFormFeed() {
    check("\" \\f \"");
  }

  @Test
  public void testUndefinedEscape() {
    error("\" \\d \"");
  }

  @Test
  public void testUnfinishedUnicodeEscape1() {
    error("\"\\u\"");
  }

  @Test
  public void testUnfinishedUnicodeEscape2() {
    error("\"\\u0\"");
  }

  @Test
  public void testUnfinishedUnicodeEscape3() {
    error("\"\\u00\"");
  }

  @Test
  public void testUnfinishedUnicodeEscape4() {
    error("\"\\u004\"");
  }

  @Test
  public void testUnicodeEscape() {
    check("\"\\u0061\"");
  }

  @Test
  public void testUnicodeEscapeLowerCase() {
    check("\"\\u00ff\"");
  }

  @Test
  public void testUnicodeEscapeUpperCase() {
    check("\"\\u00FF\"");
  }

  @Test
  public void testMustAllowZero() {
    check("0");
  }

  @Test
  public void testMustAllowZeroPointZero() {
    check("0.0");
  }

  @Test
  public void testInitialZeroInNumber() {
    error("0123");
  }

  @Test
  public void testInitialZeroInNegativeNumber() {
    error("-0123");
  }

  @Test
  public void testInitialZeroInFloat() {
    error("0123.0");
  }

  @Test
  public void testInitialZeroInNegativeFloat() {
    error("-0123.0");
  }

  @Test
  public void test_eFloatPlus() {
    check("1e+1");
  }

  @Test
  public void test_EFloatPlus() {
    check("1E+1");
  }

  @Test
  public void test_eFloatMinus() {
    check("1e-1");
  }

  @Test
  public void test_EFloatMinus() {
    check("1E-1");
  }

  @Test
  public void test_eFloat() {
    check("1e1");
  }

  @Test
  public void test_EFloat() {
    check("1E1");
  }

  private void check(String json) {
    try {
      Expression expr = Parser.compile(json);
      JsonNode actual = expr.apply(null);

      JsonNode expected = mapper.readTree(json);

      assertEquals("actual class " + actual.getClass() + ", expected class " + expected.getClass(), expected, actual);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void error(String json) {
    try {
      Parser.compile(json);
      fail("Successfully parsed " + json);
    } catch (JstlException e) {
      // this is what we want
    }
  }

}
