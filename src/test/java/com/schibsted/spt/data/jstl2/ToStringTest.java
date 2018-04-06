
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Verifying that toString works as it should.
 */
public class ToStringTest extends TestBase {

  // ----- DOT EXPRESSIONS

  @Test
  public void testDot() {
    verify(".", ".");
  }

  @Test
  public void testDotKey() {
    verify(".key", ".key");
  }

  @Test
  public void testDotKeyDotKey() {
    verify(".key.foo", ".key.foo");
  }

  // ----- LITERALS

  @Test
  public void testNumber() {
    verify("22", "22");
  }

  // ----- FUNCTIONS

  @Test
  public void testFunction0() {
    verify("now()", "now()");
  }

  @Test
  public void testFunction() {
    verify("is-number(22)", "is-number(22)");
  }

  @Test
  public void testFunction2() {
    verify("number(\"22\", null)", "number(\"22\", null)");
  }

  // ----- OPERATORS

  @Test
  public void testTwoPlusTwo() {
    verify("2+2", "2 + 2");
  }

  // ----- FOR

  @Test
  public void testFor() {
    verify("[for (.foo) .bar]", "[for (.foo) .bar]");
  }

  // ----- VARIABLE

  @Test
  public void testVariable() {
    verify("$foo", "$foo");
  }

  // ----- UTILITIES

  private void verify(String input, String output) {
    Expression expr = Parser.compile(input);
    String actual = expr.toString();

    assertEquals(output, actual);
  }

}
