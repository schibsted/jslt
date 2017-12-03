
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Test cases for function implementations.
 */
public class FunctionTest extends TestBase {

  // ===== NUMBER

  @Test
  public void testWrongType() {
    check("{}", "number([1,2,3,4])", "null");
  }

  @Test
  public void testInteger() {
    check("{}", "number(42)", "42");
  }

  @Test
  public void testDouble() {
    check("{}", "number(42.0)", "42.0");
  }

  @Test
  public void testIntegerString() {
    check("{}", "number(\"42\")", "42");
  }

  @Test
  public void testDoubleString() {
    check("{}", "number(\"42.0\")", "42.0");
  }

  @Test
  public void testNotANumber() {
    error("number(\"george\")", "george");
  }

  @Test
  public void testNoArguments() {
    error("number()", "argument");
  }

  @Test
  public void testNumberNull() {
    check("{}", "number(null)", "null");
  }

  @Test
  public void testNumberObject() {
    check("{}", "number({})", "null");
  }

  // ===== TEST

  @Test
  public void testTestMatches() {
    check("{}", "test(\"abc\", \"abc\")", "true");
  }

  @Test
  public void testTestMatchesNot() {
    check("{}", "test(\"cba\", \"abc\")", "false");
  }

  @Test
  public void testTestString() {
    check("{}", "test(23123, \"\\d+\")", "true");
  }

  @Test
  public void testTestOnNull() {
    error("test(23123, null)", "null");
  }

  // ===== CAPTURE

  @Test
  public void testCaptureNoMatch() {
    check("{}", "capture(\"abc\", \"(?<foo>\\d+)\")", "{}");
  }

  @Test
  public void testCaptureMatch() {
    check("{}", "capture(\"abc\", \"(?<foo>[a-z]+)\")", "{\"foo\":\"abc\"}");
  }

  @Test
  public void testCaptureNotAString() {
    check("{}", "capture(123456, \"(?<foo>\\d+)\")", "{\"foo\":\"123456\"}");
  }

  @Test
  public void testCaptureNotAtStart() {
    check("{}", "capture(\"abc123456def\", \"(?<foo>\\d+)\")", "{\"foo\":\"123456\"}");
  }

  @Test
  public void testCaptureNull() {
    check("{}", "capture(null, \"(?<foo>\\d+)\")", "null");
  }

  @Test
  public void testCaptureOnNull() {
    error("capture(\"blurgh\", null)", "null");
  }

  // ===== SPLIT

  @Test
  public void testSplitNull() {
    check("{}", "split(null, \",\")", "null");
  }

  @Test
  public void testSplitEmpty() {
    check("{}", "split(\"\", \",\")", "[\"\"]");
  }

  @Test
  public void testSplitOne() {
    check("{}", "split(\"abc\", \",\")", "[\"abc\"]");
  }

  @Test
  public void testSplitThree() {
    check("{}", "split(\"1,2,3\", \",\")", "[\"1\",\"2\",\"3\"]");
  }

  @Test
  public void testSplitOnNull() {
    error("split(\"1,2,3\", null)", "null");
  }

  // ===== NOT

  @Test
  public void testNotNull() {
    check("{}", "not(null)", "true");
  }

  @Test
  public void testNotFalse() {
    check("{}", "not(false)", "true");
  }

  @Test
  public void testNotTrue() {
    check("{}", "not(true)", "false");
  }

  // ===== FALLBACK

  @Test
  public void testFallbackNull() {
    check("{}", "fallback(null, true)", "true");
  }

  @Test
  public void testFallbackTrue() {
    check("{}", "fallback(true, null)", "true");
  }

  @Test
  public void testFallbackThree() {
    check("{}", "fallback(.foo, .bar, \"heyho\")", "\"heyho\"");
  }
}
