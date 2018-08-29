
package com.schibsted.spt.data.jslt;

import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Tests that cannot be expressed in JSON.
 */
public class StaticTests extends TestBase {

  @Test
  public void testExceptionWithNoLocation() {
    try {
      Expression expr = Parser.compileString("contains(2, 2)");
      JsonNode actual = expr.apply(null);
    } catch (JsltException e) {
      assertTrue(e.getSource() == null);
      assertEquals(-1, e.getLine());
      assertEquals(-1, e.getColumn());
    }
  }

  @Test
  public void testObjectKeyOrder() {
    Expression expr = Parser.compileString("{\"a\":1, \"b\":2}");
    JsonNode actual = expr.apply(null);

    Iterator<String> it = actual.fieldNames();
    assertEquals("a", it.next());
    assertEquals("b", it.next());
  }

  @Test
  public void testRandomFunction() {
    try {
      JsonNode context = mapper.readTree("{}");

      Expression expr = Parser.compileString("random()");

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

  @Test
  public void testJavaExtensionFunction() {
    check("{}", "test()", "42", Collections.EMPTY_MAP,
          Collections.singleton(new TestFunction()));
  }

  @Test
  public void testNowFunction() {
    JsonNode now1 = execute("{}", "now()");
    double now2 = System.currentTimeMillis();
    long delta = 1000; // milliseconds of wriggle-room

    assertTrue(now1.isDouble());
    assertTrue("now1 (" + now1 + ") << now2 (" + now2 + ")",
               (now1.asDouble() * 1000) < (now2 + delta));
    assertTrue("now1 (" + now1 + ") >> now2 (" + now2 + ")",
               (now1.asDouble() * 1000) > (now2 - delta));
  }

  @Test @Ignore // this takes a while to run, so we don't usually do it
  public void testRegexpCache() {
    // generate lots and lots of regular expressions, and see if we
    // manage to blow up the cache
    Expression expr = Parser.compileString("capture(\"foo\", .)");

    for (int ix = 0; ix < 10000000; ix++) {
      String r = generateRegexp();
      JsonNode regexp = new TextNode(r);
      expr.apply(regexp);
    }
  }

  private String generateRegexp() {
    if (Math.random() < 0.3) {
      // generate compound expression
      int parts = (int) (Math.random() * 5);
      StringBuilder buf = new StringBuilder();
      buf.append("(");
      for (int ix = 0; ix < parts; ix++) {
        buf.append(generateRegexp());
        if (ix + 1 < parts)
          buf.append("|");
      }
      buf.append(")");
      return buf.toString();

    } else {
      // generate simple expression
      int kind = (int) (Math.random() * 4);

      switch(kind) {
      case 0:
        return "[A-Za-z0-9]+";
      case 1:
        return makeRandomString(10);
      case 2:
        return "\\d+";
      case 3:
        return "20\\d\\d-[01]\\d-[0123]\\d";
      }
    }

    return "foo";
  }

  private String makeRandomString(int length) {
    char[] buf = new char[length];
    for (int ix = 0; ix < length; ix++)
      buf[ix++] = (char) ('a' + ((char) (Math.random() * 26)));
    return new String(buf);
  }

}
