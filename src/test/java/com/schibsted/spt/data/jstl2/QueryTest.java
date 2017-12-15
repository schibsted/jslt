
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Test cases verifying queries against an input.
 */
public class QueryTest extends TestBase {

  @Test
  public void testDot() {
    check("{}", ".", "{}");
  }

  @Test
  public void testDotKey() {
    check("{\"foo\" : 2}", ".foo", "2");
  }

  @Test
  public void testQuotedDotKey() {
    check("{\"@foo\" : 2}", ".\"@foo\"", "2");
  }

  @Test
  public void testDotNoSuchKey() {
    check("{\"foo\" : 2}", ".bar", "null");
  }

  @Test
  public void testDotKeyDotKey() {
    check("{\"foo\" : {\"bar\" : 22}}", ".foo.bar", "22");
  }

  @Test
  public void testDotKeyDotKeyWithNull() {
    check("{\"foo\" : {\"bar\" : 22}}", ".bar.bar", "null");
  }

  @Test
  public void testDotKeyDotKeyDotKey() {
    check("{\"foo\" : {\"bar\" : {\"baz\" : 221}}}", ".foo.bar.baz", "221");
  }

  @Test
  public void testIfNoElse() {
    check("{}", "if (true) 320", "320");
  }

  @Test
  public void testIfAndElse() {
    check("{}", "if (true) 320 else 240", "320");
  }

  @Test
  public void testIfFalseAndElse() {
    check("{}", "if (false) 320 else 240", "240");
  }

  @Test
  public void testNullIsFalse() {
    check("{}", "if (null) 320 else 240", "240");
  }

  @Test
  public void testFunctionCall() {
    check("{}", "number(\"22\")", "22");
  }

  @Test
  public void testNonExistentFunction() {
    error("blurgle(\"22\")", "blurgle");
  }

  @Test
  public void testVariable() {
    check("{}", "$foo", "42", makeVars("foo", "42"));
  }

  @Test
  public void testNonExistentVariable() {
    error("$blurgle", "blurgle");
  }

  @Test
  public void testFunctionCallVariableParam() {
    check("{}", "number( $num )", "22", makeVars("num", "\"22\""));
  }

  @Test
  public void testVariableDotKey() {
    check("{}", "let obj = {\"foo\" : 321} $obj.foo", "321");
  }

  @Test
  public void testVariableDotKeyDotKey() {
    check("{}", "let obj = {\"foo\" : 321} $obj.foo.bar", "null");
  }

  @Test
  public void testVariableDotKeyDotKey2() {
    check("{}", "let obj = {\"foo\" : {\"bar\":321}} $obj.foo.bar", "321");
  }

  @Test
  public void testEqualsFalse() {
    check("{}", "123 == 321", "false");
  }

  @Test
  public void testEqualsTrue() {
    check("{}", "123 == 123", "true");
  }

  @Test
  public void testEqualsAcrossTypes() {
    check("{}", "123 == 123.0", "true");
  }

  @Test
  public void testEqualsAcrossTypes2() {
    // the plus returns a LongNode, but 123 is an IntNode
    check("{}", "123 == (120 + 3)", "true");
  }

  @Test
  public void testEqualsBitMoreComplex() {
    check("{\"foo\" : {\"bar\" : 123}}", ".foo.bar == 123", "true");
  }

  @Test
  public void testEqualsBitMoreComplex2() {
    check("{\"foo\" : {\"bar\" : 123}}", "123 == .foo.bar", "true");
  }

  @Test
  public void testNotEqualsFalse() {
    check("{}", "123 != 123", "false");
  }

  @Test
  public void testNotEqualsFalseDecimal() {
    check("{}", "123 != 123.0", "false");
  }

  @Test
  public void testNotEqualsTrue() {
    check("{}", "123 != 321", "true");
  }

  @Test
  public void testBiggerOrEqualFalse() {
    check("{}", "123 >= 321", "false");
  }

  @Test
  public void testBiggerOrEqualTrue() {
    check("{}", "321 >= 123", "true");
  }

  @Test
  public void testBiggerOrEqualTrueEqual() {
    check("{}", "123 >= 123", "true");
  }

  @Test
  public void testBiggerOrEqualTrueDecimals() {
    check("{}", "321 >= 123.0", "true");
  }

  @Test
  public void testBiggerOrEqualFalseDecimals() {
    check("{}", "123.0 >= 321", "false");
  }

  @Test
  public void testBiggerOrEqualFalseString() {
    check("{}", "\"abc\" >= \"def\"", "false");
  }

  @Test
  public void testBiggerOrEqualTrueString() {
    check("{}", "\"def\" >= \"abc\"", "true");
  }

  @Test
  public void testBiggerFalse() {
    check("{}", "123 > 321", "false");
  }

  @Test
  public void testBiggerTrue() {
    check("{}", "321 > 123", "true");
  }

  @Test
  public void testBiggerTrueDecimals() {
    check("{}", "321 > 123.0", "true");
  }

  @Test
  public void testBiggerFalseDecimals() {
    check("{}", "123.0 > 321", "false");
  }

  @Test
  public void testBiggerFalseString() {
    check("{}", "\"abc\" > \"def\"", "false");
  }

  @Test
  public void testBiggerTrueString() {
    check("{}", "\"def\" > \"abc\"", "true");
  }

  @Test
  public void testSmallerFalse() {
    check("{}", "321 < 123", "false");
  }

  @Test
  public void testSmallerTrue() {
    check("{}", "123 < 321", "true");
  }

  @Test
  public void testSmallerTrueDecimals() {
    check("{}", "123.0 < 321", "true");
  }

  @Test
  public void testSmallerFalseDecimals() {
    check("{}", "321 < 123.0", "false");
  }

  @Test
  public void testSmallerFalseString() {
    check("{}", "\"def\" < \"abc\"", "false");
  }

  @Test
  public void testSmallerTrueString() {
    check("{}", "\"abc\" < \"def\"", "true");
  }

  @Test
  public void testSmallerOrEqualTrue() {
    check("{}", "123 <= 321", "true");
  }

  @Test
  public void testSmallerOrEqualFalse() {
    check("{}", "321 <= 123", "false");
  }

  @Test
  public void testSmallerOrEqualTrueEqual() {
    check("{}", "123 <= 123", "true");
  }

  @Test
  public void testSmallerOrEqualFalseDecimals() {
    check("{}", "321 <= 123.0", "false");
  }

  @Test
  public void testSmallerOrEqualTrueDecimals() {
    check("{}", "123.0 <= 321", "true");
  }

  @Test
  public void testSmallerOrEqualTrueString() {
    check("{}", "\"abc\" <= \"def\"", "true");
  }

  @Test
  public void testSmallerOrEqualFalseString() {
    check("{}", "\"def\" <= \"abc\"", "false");
  }

  // ===== '+' OPERATOR

  @Test
  public void testPlusForStrings() {
    check("{}", "\"foo\" + \"bar\"", "\"foobar\"");
  }

  @Test
  public void testPlusStringNull() {
    check("{}", "\"foo\" + null", "\"foonull\"");
  }

  @Test
  public void testPlusStringNumber() {
    check("{}", "\"foo\" + 5", "\"foo5\"");
  }

  @Test
  public void testPlusThree() {
    check("{}", "\"foo\" + \"bar\" + \"baz\"", "\"foobarbaz\"");
  }

  @Test
  public void testPlusFunction() {
    check("{}", "\"foo\" + number(5)", "\"foo5\"");
  }

  @Test
  public void testPlusInts() {
    check("{}", "22 + 18", "40");
  }

  @Test
  public void testPlusDecimals() {
    check("{}", "2.2 + 1.8", "4.0");
  }

  @Test
  public void testPlusNumberNull() {
    check("{}", "2 + null", "null");
  }

  @Test
  public void testPlusArrays() {
    check("{}", "[1,2,3] + [4,5]", "[1,2,3,4,5]");
  }

  @Test
  public void testPlusEmptyArray() {
    check("{}", "[] + [4,5]", "[4,5]");
  }

  @Test
  public void testPlusNumberAndArray() {
    error("232 + [4,5]", "convert");
  }

  @Test
  public void testPlusNumberAndObject() {
    error("232 + {\"foo\":34}", "convert");
  }

  @Test
  public void testPlusNumberAndBoolean() {
    error("232 + false", "convert");
  }

  @Test
  public void testPlusObjects() {
    check("{}", ". + {\"foo\":14}", "{\"foo\":14}");
  }

  @Test
  public void testPlusObjectsUnion() {
    check("{\"bar\":44}", ". + {\"foo\":14}", "{\"foo\":14,\"bar\":44}");
  }

  @Test
  public void testPlusObjectsUnionLeftSideWins() {
    check("{\"bar\":44,\"foo\":false}", ". + {\"foo\":14}",
          "{\"foo\":false,\"bar\":44}");
  }

  // ===== '*' OPERATOR

  @Test
  public void testMultiplyString() {
    check("{}", "\"foo\" * 3", "\"foofoofoo\"");
  }

  @Test
  public void testMultiplyInts() {
    check("{}", "3 * 5", "15");
  }

  @Test
  public void testMultiplyDoubles() {
    check("{}", "2.5 * 4.0", "10.0");
  }

  @Test
  public void testMultiplyIntWithDouble() {
    check("{}", "2.5 * 4", "10.0");
  }

  @Test
  public void testMultiplyThree() {
    check("{}", "5 * 2.5 * 4", "50.0");
  }

  @Test
  public void testMultiplyNull() {
    check("{}", "2.5 * null", "null");
  }

  // ===== '-' OPERATOR

  @Test
  public void testMinusInts() {
    check("{}", "22 - 18", "4");
  }

  @Test
  public void testMinusDecimals() {
    // the problem here is that the test fails because the result is
    check("{}", "2.5 - 1.8", "0.7");
  }

  @Test
  public void testMinusNumberNull() {
    check("{}", "2 - null", "null");
  }

  @Test
  public void testMinusNonNumber() {
    error("232 - false", "number");
  }

  @Test
  public void testMinusString() {
    error("\"foo\" - 22", "number");
  }

  @Test @Disabled // not sure how to fix this
  public void testMinusThree() {
    check("{}", "22 - 18 - 2", "2");
  }

  // ===== '-' OPERATOR

  @Test
  public void testDivideIntegers() {
    check("{}", "16 / 4", "4");
  }

  @Test
  public void testDivideFloats() {
    check("{}", "16.0 / 32.0", "0.5");
  }

  @Test
  public void testDivideToFloat() {
    check("{}", "16 / 32", "0.5");
  }

  @Test
  public void testDivideString() {
    error("\"foo\" / 22", "number");
  }

  // ===== OPERATOR PRECEDENCE

  @Test
  public void testMultiplyThenAdd() {
    check("{}", "2 + 2 * 5", "12");
  }

  @Test
  public void testMultiplyThenAdd2() {
    check("{}", "5 * 2 + 2", "12");
  }

  @Test
  public void testMultiplyThenSubtract() {
    check("{}", "20 - 2 * 5", "10");
  }

  @Test
  public void testDivideThenSubtract() {
    check("{}", "20 - 10 / 2", "15");
  }

  // ===== ...

  @Test
  public void testIfAddPrecedence() {
    check("{}", "if (false) \"5\" else \"foo\" + \"bar\"", "\"foobar\"");
  }

  @Test
  public void testIfAddPrecedence2() {
    check("{}", "if (true) \"5\" else \"foo\" + \"bar\"", "\"5\"");
  }

  @Test
  public void testParentheses() {
    check("{}", "(if (true) \"5\" else \"foo\") + \"bar\"", "\"5bar\"");
  }

  @Test
  public void testParenthesesInLet() {
    check("{}", "let foo = (5)\n2", "2");
  }

  @Test
  public void testArrayIndexing() {
    check("[1,2,3,4,5]", ".[0]", "1");
  }

  @Test
  public void testFunctionArrayIndexing() {
    check("[]", "split(\"a,b,c,d\", \",\")[0]", "\"a\"");
  }

  @Test
  public void testArraySlicing() {
    check("[1,2,3,4,5]", ".[1 : 4]", "[2,3,4]");
  }

  @Test
  public void testArraySlicingFromStart() {
    check("[1,2,3,4,5]", ".[ : 4]", "[1,2,3,4]");
  }

  @Test
  public void testArraySlicingToEnd() {
    check("[1,2,3,4,5]", ".[1 : ]", "[2,3,4,5]");
  }

  @Test
  public void testArraySlicingSkipOne() {
    check("[1,2,3,4,5]", ".[ : -1]", "[1,2,3,4]");
  }

  @Test
  public void testArraySlicingTooFar() {
    check("[1,2,3,4,5]", ".[ : 20]", "[1,2,3,4,5]");
  }

  @Test
  public void testStringIndexing() {
    check("\"12345\"", ".[0]", "\"1\"");
  }

  @Test
  public void testStringIndexingBeyondTheEnd() {
    error("\"12345\"", ".[7]", "index");
  }

  @Test
  public void testStringSlicing() {
    check("\"12345\"", ".[1 : 4]", "\"234\"");
  }

  @Test
  public void testStringSlicingToEnd() {
    check("\"12345\"", ".[1 : ]", "\"2345\"");
  }

  @Test
  public void testStringSlicingSkipOne() {
    check("\"12345\"", ".[ : -1]", "\"1234\"");
  }

  @Test
  public void testStringSlicingTooFar() {
    check("\"12345\"", ".[ : 20]", "\"12345\"");
  }

  @Test
  public void testForLoop() {
    check("[\"1\", \"2\", \"3\"]", "for (.) number(.)", "[1,2,3]");
  }

  @Test
  public void testForLoopEmpty() {
    check("[]", "for (.) number(.)", "[]");
  }

  @Test
  public void testForLoopNull() {
    check("[]", "for (null) number(.)", "null");
  }

  @Test
  public void testForOnFunctionMapObject() {
    check("[]",
          "for (split(\"1,2,3,4\", \",\")) { "+
          "  \"number\" : number(.) " +
          "}", "[{\"number\":1},{\"number\":2},{\"number\":3},{\"number\":4}]");
  }

  @Test
  public void testOrTrue() {
    check("{\"foo\" : \"bar\"}",
          ".foo or true",
          "true");
  }

  @Test
  public void testOrFalse() {
    check("{\"foo\" : \"bar\"}",
          ".foo or false",
          "true");
  }

  @Test
  public void testOrFalse2() {
    check("{\"foo\" : \"bar\"}",
          "false or .foo",
          "true");
  }

  @Test
  public void testOrFalseFalse() {
    check("{\"foo\" : \"bar\"}",
          ".bar or false",
          "false");
  }

  @Test
  public void testOrThree() {
    check("{\"foo\" : \"bar\"}",
          ".bar or true or .foo",
          "true");
  }

  @Test
  public void testAndComparison() {
    check("{\"foo\" : \"bar\"}",
          ".foo == \"bar\" and .foo != \"baz\"",
          "true");
  }

  @Test
  public void testAndTrue() {
    check("{\"foo\" : \"bar\"}",
          ".foo and true",
          "true");
  }

  @Test
  public void testAndFalse() {
    check("{\"foo\" : \"bar\"}",
          ".foo and false",
          "false");
  }

  @Test
  public void testAndOrFalse() {
    check("{\"foo\" : \"bar\"}",
          ".foo and false or .foo and true",
          "true");
  }

  @Test
  public void testAndOrTrue() {
    check("{\"foo\" : \"bar\"}",
          ".foo and false or .bar and true",
          "false");
  }

  @Test
  public void testAndThree() {
    check("{\"foo\" : \"bar\"}",
          ".foo and true and 22",
          "true");
  }
}
