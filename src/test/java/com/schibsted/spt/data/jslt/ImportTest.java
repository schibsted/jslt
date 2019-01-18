
package com.schibsted.spt.data.jslt;

import java.util.Collections;
import org.junit.Test;

/**
 * Test cases for imports.
 */
public class ImportTest extends TestBase {

  @Test
  public void testIdFunction() {
    check("{}", "import \"module.jstl\" as m " +
                "m:id(5) ", "5");
  }

  @Test
  public void testModuleFunction() {
    check("{}", "import \"module-body.jstl\" as m " +
                "m(5) ", "27");
  }

  @Test
  public void testModuleNoBodyFunction() {
    error("import \"module.jstl\" as m " +
          "m(5) ", "body");
  }

  @Test
  public void testImportGraph() {
    // we import A -> B, A -> C -> B, and this should be fine
    check("{}", "import \"a-1.jstl\" as a " +
                "a(.)", "1");
  }

  @Test
  public void testCyclicImport() {
    error("import \"a-2.jstl\" as m " +
          "m(5) ", "already imported");
  }

  @Test
  public void testImportUsesExtensionFunction() {
    check("{}",
          "import \"uses-test.jstl\" as u " +
          "u(.)",
          "42", Collections.EMPTY_MAP,
          Collections.singleton(new TestFunction()));
  }

  // --- test the count function

  @Test
  public void testCountKeys() {
    check("{}",
          "import \"functions.jstl\" as f " +
          "f:count(.) ", "0");
  }

  @Test
  public void testCountKeysNumber() {
    check("0",
          "import \"functions.jstl\" as f " +
          "f:count(.) ", "0");
  }

  @Test
  public void testCountKeysOne() {
    check("{\"foo\" : 0}",
          "import \"functions.jstl\" as f " +
          "f:count(.) ", "1");
  }

  @Test
  public void testCountKeysTwo() {
    check("{\"foo\" : 0, \"bar\" : 1}",
          "import \"functions.jstl\" as f " +
          "f:count(.) ", "2");
  }

  @Test
  public void testCountKeysTwoRecursive() {
    check("{\"foo\" : 0, \"bar\" : {\"baz\" : []}}",
          "import \"functions.jstl\" as f " +
          "f:count(.) ", "3");
  }

  @Test
  public void testCountKeysTwoRecursiveArray() {
    check("{\"foo\" : 0, \"bar\" : {\"baz\" : [{\"foo\" : 0, \"bar\" : 1}]}}",
          "import \"functions.jstl\" as f " +
          "f:count(.) ", "5");
  }

  // FIXME: verify that function passed in to top-level parser is also
  // available when parsing imported modules
}
