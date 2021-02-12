
package com.schibsted.spt.data.jslt;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test cases for the function wrapper implementations.
 */
public class FunctionWrapperTest extends TestBase {

  @Test
  public void testWrapStaticMethod() throws Exception {
    Collection<Function> functions = Collections.singleton(
      FunctionUtils.wrapStaticMethod("url-decode",
                                     "java.net.URLDecoder", "decode",
                                     new Class[] {String.class, String.class})
    );

    check("{}", "url-decode(\"foo\", \"utf-8\")", "\"foo\"",
          Collections.EMPTY_MAP,
          functions);
  }

  @Test
  public void testWrapStaticMethodNotFound() throws Exception {
    try {
      FunctionUtils.wrapStaticMethod("url-decode",
                                     "java.net.URLDecoder", "decooode");
      fail("accepted non-existent method");
    } catch (JsltException e) {
      // this is what we expected
    }
  }

  @Test
  public void testWrapStaticMethodLong() throws Exception {
    Collection<Function> functions = Collections.singleton(
      FunctionUtils.wrapStaticMethod("time-millis",
                                     "java.lang.System", "currentTimeMillis")
    );
    String query = "time-millis()";

    long before = System.currentTimeMillis();

    JsonNode context = mapper.readTree("{}");
    Expression expr = Parser.compileString(query, functions);
    JsonNode actual = expr.apply(context);
    long value = actual.asLong();

    long after = System.currentTimeMillis();

    assertTrue(before <= value);
    assertTrue(value <= after);
  }

  @Test
  public void testWrapStaticMethodNumeric() throws Exception {
    Collection<Function> functions = Collections.singleton(
      FunctionUtils.wrapStaticMethod("pow",
                                     "java.lang.Math", "pow")
    );
    String query = "pow(2, 10)";

    JsonNode context = mapper.readTree("{}");
    Expression expr = Parser.compileString(query, functions);
    JsonNode actual = expr.apply(context);

    assertTrue(actual.asInt() == 1024);
  }
}
