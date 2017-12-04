
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

// FIXME: nested if else ambiguity -> no way to handle for user
//  -> solve by introducing parentheses

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
  public void testPlusForStrings() {
    check("{}", "\"foo\" + \"bar\"", "\"foobar\"");
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
  public void testOrFalseFalse() {
    check("{\"foo\" : \"bar\"}",
          ".bar or false",
          "false");
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
