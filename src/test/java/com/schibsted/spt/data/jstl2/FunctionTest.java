
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.IntNode;

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
  public void testTestMatchesPart() {
    check("{}", "test(\"abcdef\", \"abc\")", "true");
  }

  @Test
  public void testTestMatchesNot() {
    check("{}", "test(\"cba\", \"abc\")", "false");
  }

  @Test
  public void testTestString() {
    check("{}", "test(23123, \"\\\\d+\")", "true");
  }

  @Test
  public void testTestOnNull() {
    error("test(23123, null)", "null");
  }

  // ===== CAPTURE

  @Test
  public void testCaptureNoMatch() {
    check("{}", "capture(\"abc\", \"(?<foo>\\\\d+)\")", "{}");
  }

  @Test
  public void testCaptureMatch() {
    check("{}", "capture(\"abc\", \"(?<foo>[a-z]+)\")", "{\"foo\":\"abc\"}");
  }

  @Test
  public void testCaptureNotAString() {
    check("{}", "capture(123456, \"(?<foo>\\\\d+)\")", "{\"foo\":\"123456\"}");
  }

  @Test
  public void testCaptureNotAtStart() {
    check("{}", "capture(\"abc123456def\", \"(?<foo>\\\\d+)\")", "{\"foo\":\"123456\"}");
  }

  @Test
  public void testCaptureNull() {
    check("{}", "capture(null, \"(?<foo>\\\\d+)\")", "null");
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

  // ===== JOIN

  @Test
  public void testJoinNull() {
    check("{}", "join(null, \",\")", "null");
  }

  @Test
  public void testJoinEmpty() {
    check("{}", "join([], \",\")", "\"\"");
  }

  @Test
  public void testJoinOne() {
    check("{}", "join([1], \",\")", "\"1\"");
  }

  @Test
  public void testJoinTwo() {
    check("{}", "join([1,2], \", \")", "\"1, 2\"");
  }

  @Test
  public void testJoinThree() {
    check("{}", "join([1,2,3], \", \")", "\"1, 2, 3\"");
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

  @Test
  public void testFallbackIsAMagicMacro() {
    // if fallback were a function evaluation of the second parameter
    // would fail. however, it's a macro, and since the first
    // parameter is a value, we never evaluate the second one. which
    // is what this test verifies.
    check("{}", "fallback(22, true + false)", "22");
  }

  // ===== IS-OBJECT

  @Test
  public void testIsObjectTrue() {
    check("{}", "is-object(.)", "true");
  }

  @Test
  public void testIsObjectFalse() {
    check("{}", "is-object([1,2,3])", "false");
  }

  // ===== IS-ARRAY

  @Test
  public void testIsArrayTrue() {
    check("{}", "is-array([1,2,3])", "true");
  }

  @Test
  public void testIsArrayFalse() {
    check("{}", "is-array(.)", "false");
  }

  // ===== IS-STRING

  @Test
  public void testIsStringTrue() {
    check("{}", "is-string(\"[1,2,3]\")", "true");
  }

  @Test
  public void testIsStringFalse() {
    check("{}", "is-string(.)", "false");
  }

  // ===== LOWERCASE

  @Test
  public void testLowercaseString() {
    check("{}", "lowercase(\"FOO\")", "\"foo\"");
  }

  @Test
  public void testLowercaseNumber() {
    check("{}", "lowercase(22)", "\"22\"");
  }

  @Test
  public void testLowercaseNull() {
    check("{}", "lowercase(null)", "null");
  }

  // ===== STARTS-WITH

  @Test
  public void testStartsWithNull() {
    check("{}", "starts-with(null, \"hey\")", "false");
  }

  @Test
  public void testStartsWithTrue() {
    check("{}", "starts-with(\"heypådey\", \"hey\")", "true");
  }

  @Test
  public void testStartsWithFalse() {
    check("{}", "starts-with(\"heipådeg\", \"hey\")", "false");
  }

  // ===== ENDS-WITH

  @Test
  public void testEndsWithNull() {
    check("{}", "ends-with(null, \"hey\")", "false");
  }

  @Test
  public void testEndsWithTrue() {
    check("{}", "ends-with(\"heypådey\", \"dey\")", "true");
  }

  @Test
  public void testEndsWithFalse() {
    check("{}", "ends-with(\"heipådeg\", \"dey\")", "false");
  }

  // ===== CONTAINS

  @Test
  public void testContainsEmpty() {
    check("{}", "contains(\"Type\", [])", "false");
  }

  @Test
  public void testContainsFalse() {
    check("{}", "contains(\"Type\", [\"Taip\"])", "false");
  }

  @Test
  public void testContainsNull() {
    check("{}", "contains(null, [\"Taip\"])", "false");
  }

  @Test
  public void testContainsTrue() {
    check("{}", "contains(\"Taip\", [\"foo\", \"Taip\", \"halp\"])", "true");
  }

  // ===== SIZE

  @Test
  public void testSizeArray() {
    check("{}", "size([1,2,3])", "3");
  }

  @Test
  public void testSizeObject() {
    check("{}", "size({\"foo\" : 2, \"bar\" : 24})", "2");
  }

  @Test
  public void testSizeNull() {
    check("{}", "size( .nonexistent )", "null");
  }

  @Test
  public void testSizeString() {
    check("{}", "size(\"Lars Marius\")", "11");
  }

  @Test
  public void testSizeBoolean() {
    error("size(true)", "true");
  }

  // ===== STRING

  @Test
  public void testBooleanToString() {
    check("{}", "string(false)", "\"false\"");
  }

  @Test
  public void testNumberToString() {
    check("{}", "string(123.2)", "\"123.2\"");
  }

  @Test
  public void testStringToString() {
    check("{}", "string(\"foo\")", "\"foo\"");
  }

  @Test
  public void testNullToString() {
    check("{}", "string(null)", "\"null\"");
  }

  // ===== EXTENSION FUNCTION

  private static class TestFunction implements Function {

    public String getName() {
      return "test";
    }

    public int getMinArguments() {
      return 0;
    }

    public int getMaxArguments() {
      return 0;
    }

    public JsonNode call(JsonNode input, JsonNode[] params) {
      return new IntNode(42);
    }
  }

  @Test
  public void testTestFunction() {
    check("{}", "test()", "42", Collections.EMPTY_MAP,
          Collections.singleton(new TestFunction()));
  }
}
