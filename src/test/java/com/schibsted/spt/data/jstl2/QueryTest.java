
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
  public void testDotNoSuchKey() {
    check("{\"foo\" : 2}", ".bar", "null");
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

}
