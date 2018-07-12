
package com.schibsted.spt.data.jslt;

import java.util.Iterator;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

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
