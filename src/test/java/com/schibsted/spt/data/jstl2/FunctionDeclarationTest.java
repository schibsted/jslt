
package com.schibsted.spt.data.jstl2;

import org.junit.Test;

/**
 * Test cases for function declarations.
 */
public class FunctionDeclarationTest extends TestBase {

  @Test
  public void testIdFunction() {
    check("{}", "def id(param) $param " +
                "id(5) ", "5");
  }

  @Test
  public void testPlusFunction() {
    check("{}", "def plus(n1, n2) $n1 + $n2 " +
                "plus(2, 3)", "5");
  }

  @Test
  public void testLengthFunction() {
    check("{}",
          "def length(array) " +
          "  if ($array) " +
          "    1 + length($array[1 : ]) " +
          "  else " +
          "    0 " +
          "length([1, 2, 3, 4, 5, 6])", "6");
  }

  @Test
  public void testRedefineBuiltin() {
    check("{}",
          "def size(array) " +
          "  5 // all arrays have 5 elements\n " +
          "size([1, 2])", "5");
  }

  @Test
  public void testMutualRecursion() {
    check("{}",
          "def a(value) " +
          "  if ($value > 0) " +
          "    b($value - 1) " +
          "  else " +
          "    $value " +
          "def b(value) " +
          "  if ($value > 0) " +
          "    a($value - 2) " +
          "  else " +
          "    $value - 1 " +
          "a(5)", "-1");
  }

  @Test
  public void testTooManyParameters() {
    error("def a() " +
          "  25 " +
          "a(5)", "arguments");
  }

  @Test
  public void testBizarreParsingBug() {
    check("{}",
          "let var = 2 " +
          "def a() " +
          "  25 " +
          "a()", "25");
  }

  // FIXME: no support for optional parameters
  // FIXME: cannot refer to global variables. is this good or bad?

}
