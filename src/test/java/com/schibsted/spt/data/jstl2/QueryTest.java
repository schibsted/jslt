
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

}
