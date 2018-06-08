
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test cases for function implementations.
 */
public class FunctionTest extends TestBase {

  // ===== NUMBER

  @Test
  public void testWrongType() {
    error("number([1,2,3,4])", "[1,2,3,4]");
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
  public void testNotANumberFallback() {
    check("{}", "number(\"hurble\", [1,2,3])", "[1,2,3]");
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
    error("number({})", "{}");
  }

  // ===== ROUND

  @Test
  public void testRoundWrongType() {
    error("round([1,2,3,4])", "number");
  }

  @Test
  public void testRoundInteger() {
    check("{}", "round(42)", "42");
  }

  @Test
  public void testRoundDouble() {
    check("{}", "round(42.0)", "42");
  }

  @Test
  public void testRoundDown() {
    check("{}", "round(42.4)", "42");
  }

  @Test
  public void testRoundUp() {
    check("{}", "round(42.5)", "43");
  }

  @Test
  public void testRoundNull() {
    check("{}", "round(null)", "null");
  }

  // ===== CEILING

  @Test
  public void testCeilingWrongType() {
    error("ceiling([1,2,3,4])", "number");
  }

  @Test
  public void testCeilingInteger() {
    check("{}", "ceiling(42)", "42");
  }

  @Test
  public void testCeilingDouble() {
    check("{}", "ceiling(42.0)", "42");
  }

  @Test
  public void testCeilingDown() {
    check("{}", "ceiling(42.4)", "43");
  }

  @Test
  public void testCeilingUp() {
    check("{}", "ceiling(42.5)", "43");
  }

  @Test
  public void testCeilingNull() {
    check("{}", "ceiling(null)", "null");
  }

  // ===== FLOOR

  @Test
  public void testFloorWrongType() {
    error("floor([1,2,3,4])", "number");
  }

  @Test
  public void testFloorInteger() {
    check("{}", "floor(42)", "42");
  }

  @Test
  public void testFloorDouble() {
    check("{}", "floor(42.0)", "42");
  }

  @Test
  public void testFloorDown() {
    check("{}", "floor(42.4)", "42");
  }

  @Test
  public void testFloorUp() {
    check("{}", "floor(42.5)", "42");
  }

  @Test
  public void testFloorNull() {
    check("{}", "floor(null)", "null");
  }

  // ===== RANDOM

  @Test
  public void testRandom() {
    try {
      JsonNode context = mapper.readTree("{}");

      Expression expr = Parser.compile("random()");

      for (int ix = 0; ix < 10; ix++) {
        JsonNode actual = expr.apply(context);
        assertTrue(actual.isNumber());
        double value = actual.doubleValue();
        assertTrue(value > 0.0 && value < 1.0);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

  @Test
  public void testNotOfANumber() {
    check("{}", "not(123)", "false");
  }

  // ===== BOOLEAN

  @Test
  public void test2BooleanNull() {
    check("{}", "boolean(null)", "false");
  }

  @Test
  public void test2BooleanTrue() {
    check("{}", "boolean(true)", "true");
  }

  @Test
  public void test2BooleanFalse() {
    check("{}", "boolean(false)", "false");
  }

  @Test
  public void test2BooleanZero() {
    check("{}", "boolean(0)", "false");
  }

  @Test
  public void test2BooleanZeroDecimal() {
    check("{}", "boolean(0.0)", "false");
  }

  @Test
  public void test2BooleanNonZeroDecimal() {
    check("{}", "boolean(0.2)", "true");
  }

  @Test
  public void test2BooleanNumber() {
    check("{}", "boolean(23)", "true");
  }

  @Test
  public void test2BooleanString() {
    check("{}", "boolean(\"furdle\")", "true");
  }

  @Test
  public void test2BooleanEmptyString() {
    check("{}", "boolean(\"\")", "false");
  }

  @Test
  public void test2BooleanArray() {
    check("{}", "boolean([\"furdle\"])", "true");
  }

  @Test
  public void test2BooleanEmptyArray() {
    check("{}", "boolean([])", "false");
  }

  @Test
  public void test2BooleanObject() {
    check("{}", "boolean({\"furdle\" : 22})", "true");
  }

  @Test
  public void test2BooleanEmptyObject() {
    check("{}", "boolean({})", "false");
  }

  // ===== IS-BOOLEAN

  @Test
  public void testIsBooleanNull() {
    check("{}", "is-boolean(null)", "false");
  }

  @Test
  public void testIsBooleanTrue() {
    check("{}", "is-boolean(true)", "true");
  }

  @Test
  public void testIsBooleanFalse() {
    check("{}", "is-boolean(false)", "true");
  }

  @Test
  public void testIsBooleanZero() {
    check("{}", "is-boolean(0)", "false");
  }

  @Test
  public void testIsBooleanNumber() {
    check("{}", "is-boolean(22)", "false");
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

  // ===== ARRAY

  @Test
  public void testArrayToArray() {
    check("{}", "array([1,2,3])", "[1,2,3]");
  }

  @Test
  public void testNullToArray() {
    check("{}", "array(.missing)", "null");
  }

  @Test
  public void testBooleanToArray() {
    error("array(true)", "true");
  }

  @Test
  public void testObjectToArray() {
    check("{\"foo\" : 2}", "array(.)", "[{\"key\" : \"foo\", \"value\" : 2}]");
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

  // ===== IS-STRING

  @Test
  public void testIsNumberInteger() {
    check("{}", "is-number(232)", "true");
  }

  @Test
  public void testIsNumberDecimal() {
    check("{}", "is-number(232.0)", "true");
  }

  @Test
  public void testIsNumberFalse() {
    check("{}", "is-number(.)", "false");
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

  // ===== UPPERCASE

  @Test
  public void testUppercaseString() {
    check("{}", "uppercase(\"foo\")", "\"FOO\"");
  }

  @Test
  public void testUppercaseNumber() {
    check("{}", "uppercase(22)", "\"22\"");
  }

  @Test
  public void testUppercaseNull() {
    check("{}", "uppercase(null)", "null");
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

  @Test
  public void testContainsStringTrue() {
    check("{}", "contains(\"Taip\", \"StringWithTaip\")", "true");
  }

  @Test
  public void testContainsStringFalse() {
    check("{}", "contains(\"Taip\", \"StringWithType\")", "false");
  }

  @Test
  public void testContainsStringNull() {
    check("{}", "contains(null, \"StringnullWithType\")", "false");
  }

  @Test
  public void testContainsObjectTrue() {
    check("{}", "contains(\"Taip\", {\"Taip\" : 44})", "true");
  }

  @Test
  public void testContainsObjectFalse() {
    check("{}", "contains(\"Taip\", {\"Type\" : 44})", "false");
  }

  @Test // FIXME: not 100% sure about this
  public void testContainsObjectNonstring() {
    check("{}", "contains(42, {\"Taip\" : 44})", "false");
  }

  @Test
  public void testContainsObjectNull() {
    check("{}", "contains(null, {})", "false");
  }

  @Test
  public void testContainsBadType() {
    error("contains(123, false)", "false");
  }

  @Test
  public void testContainsStringInNull() {
    check("{}", "contains(\"Taip\", null)", "false");
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

  // ===== NOW

  @Test
  public void testNow() {
    JsonNode now1 = execute("{}", "now()");
    double now2 = System.currentTimeMillis();
    long delta = 1000; // milliseconds of wriggle-room

    assertTrue(now1.isDouble());
    assertTrue("now1 (" + now1 + ") << now2 (" + now2 + ")",
               (now1.asDouble() * 1000) < (now2 + delta));
    assertTrue("now1 (" + now1 + ") >> now2 (" + now2 + ")",
               (now1.asDouble() * 1000) > (now2 - delta));
  }

  // ===== PARSE-TIME

  @Test
  public void testParseDateTime() {
    check("\"1973-12-25 04:21:33Z\"",
          "parse-time(., \"yyyy-MM-dd HH:mm:ssX\")",
          "125641293.0");
  }

  @Test
  public void testParseDateTimeNoTimezone() {
    // in this case the code assumes UTC
    check("\"1973-12-25 04:21:33\"",
          "round(parse-time(., \"yyyy-MM-dd HH:mm:ss\"))",
          "125641293");
  }

  @Test
  public void testParseDateTimeCET() {
    check("\"1973-12-25 05:21:33CET\"", // CET, one hour later = same time UTC
          "round(parse-time(., \"yyyy-MM-dd HH:mm:ssz\"))",
          "125641293");
  }

  @Test
  public void testParseDateTimeError() {
    // no time zone
    error("parse-time(\"1973-12-25 05:22:33\", \"yyyy-MM-dd HH:mm:ssz\")",
          "Unparseable");
  }

  @Test
  public void testParseDateTimeErrorInFormat() {
    // bad format string
    error("parse-time(\"1973-12-25 05:22:33\", \"yyyy-MM-dd gnugugg HH:mm:ssz\")",
          "Couldn't parse format");
  }

  @Test
  public void testParseDate() {
    check("\"1973-12-25\"",
          "round(parse-time(., \"yyyy-MM-dd\"))",
          "125625600");
  }

  @Test
  public void testParseDateTimeNull() {
    check("null",
          "parse-time(., \"yyyy-MM-dd HH:mm:ssX\")",
          "null");
  }

  @Test
  public void testParseDateTimeMillis() {
    check("\"1973-12-25 04:21:33.123\"",
          "round(parse-time(., \"yyyy-MM-dd HH:mm:ss.S\") * 100)",
          "12564129312");
  }

  @Test
  public void testParseDateTimeErrorFallback() {
    // no time zone
    check("{}",
          "parse-time(\"1973-12-25 05:22:33\", \"yyyy-MM-dd HH:mm:ssz\", null)",
          "null");
  }

  @Test
  public void testParseDateTimeErrorInFormatFallback() {
    // bad format string
    error("parse-time(\"1973-12-25 05:22:33\", \"yyyy-MM-dd gnugugg HH:mm:ssz\", null)",
          "Couldn't parse format");
  }

  // ===== FORMAT-TIME

  @Test
  public void testFormatDateTime() {
    check("125641293.0",
          "format-time(., \"yyyy-MM-dd HH:mm:ss\")",
          "\"1973-12-25 04:21:33\"");
  }

  @Test
  public void testFormatDateTimeErrorInFormat() {
    error("format-time(125641293.0, \"yyyy-MM-dd dfkdfjkdf HH:mm:ss\")",
          "Couldn't parse format");
  }

  @Test
  public void testFormatDateTimeToUTC() {
    check("125641293.0",
          "format-time(., \"yyyy-MM-dd HH:mm:ssz\")",
          "\"1973-12-25 04:21:33UTC\"");
  }

  @Test
  public void testFormatDateTimeToCET() {
    check("125641293.0",
          "format-time(., \"yyyy-MM-dd HH:mm:ssz\", \"CET\")",
          "\"1973-12-25 05:21:33CET\"");
  }

  @Test
  public void testFormatDateTimeBadTimeZone() {
    error("format-time(125641293.0, \"yyyy-MM-dd HH:mm:ss\", \"fjsjdjsjs\")",
          "Unknown timezone");
  }

  // ===== ERROR

  @Test
  public void testErrorMessage() {
    error("error(\"Oh no\")", "Oh no");
  }

  // ===== GET-KEY

  @Test
  public void testGetKey() {
    check(" { \"foo\" : 24 } ",
          " get-key(., \"foo\") ",
          " 24 ");
  }

  @Test
  public void testGetNonexistentKey() {
    check(" { \"foo\" : 24 } ",
          " get-key(., \"bar\") ",
          " null ");
  }

  @Test
  public void testGetNullKey() {
    check(" { \"foo\" : 24 } ",
          " get-key(., null) ",
          " null ");
  }

  @Test
  public void testGetKeyFromNull() {
    check(" { \"foo\" : 24 } ",
          " get-key(null, \"key\") ",
          " null ");
  }

  @Test
  public void testGetKeyFromNonObject() {
    error(" get-key(24, \"key\") ", "24");
  }

  // ===== FROM-JSON

  @Test
  public void testFromJsonNull() {
    check(" null ",
          " from-json(.) ",
          " null ");
  }

  @Test
  public void testFromJsonJsonNull() {
    check(" \"null\" ",
          " from-json(.) ",
          " null ");
  }

  @Test
  public void testFromJsonNumber() {
    check(" \"22\" ",
          " from-json(.) ",
          " 22 ");
  }

  @Test
  public void testFromJsonComplexObject() {
    check(" \"{\\\"foo\\\" : {\\\"bar\\\" : 22}}\" ",
          " from-json(.) ",
          " {\"foo\":{\"bar\":22}} ");
  }

  @Test
  public void testFromBadJson() {
    error(" from-json(\"[1,2,\") ", "parse");
  }

  @Test
  public void testFromBadJsonFallback() {
    check(" \"[1,2,\" ",
          " from-json(., false) ",
          " false ");
  }

  @Test
  public void testFromJsonNothing() {
    check(" \"\" ", "from-json(.)", "null");
  }

  // ===== TO-JSON

  @Test
  public void testToJsonNull() {
    check(" null ",
          " to-json(.) ",
          " \"null\" ");
  }

  @Test
  public void testToJsonNumber() {
    check(" 22 ",
          " to-json(.) ",
          " \"22\" ");
  }

  @Test
  public void testToJsonComplexObject() {
    check(" {\"foo\":{\"bar\":22}} ",
          " to-json(.) ",
          " \"{\\\"foo\\\":{\\\"bar\\\":22}}\" ");
  }

  @Test
  public void testTestFunction() {
    check("{}", "test()", "42", Collections.EMPTY_MAP,
          Collections.singleton(new TestFunction()));
  }
}
