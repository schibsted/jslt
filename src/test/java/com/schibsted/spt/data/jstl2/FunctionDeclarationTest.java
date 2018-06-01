
package com.schibsted.spt.data.jstl2;

import org.junit.Test;

/**
 * Test cases for function declarations.
 */
public class FunctionDeclarationTest extends TestBase {

  String idfunc = " ";
  String plusfunc = " def id(param) $param ";

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

  // FIXME: still cannot refer to global variables. is this good or bad?

}
