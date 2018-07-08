
package com.schibsted.spt.data.jslt;

import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tests that cannot be expressed in JSON.
 */
public class StaticTests extends TestBase {

  @Test
  public void testObjectKeyOrder() {
    Expression expr = Parser.compileString("{\"a\":1, \"b\":2}");
    JsonNode actual = expr.apply(null);

    Iterator<String> it = actual.fieldNames();
    assertEquals("a", it.next());
    assertEquals("b", it.next());
  }

}
