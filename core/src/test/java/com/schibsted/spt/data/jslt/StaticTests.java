
package com.schibsted.spt.data.jslt;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;

import com.schibsted.spt.data.jslt.Module;
import com.schibsted.spt.data.jslt.impl.ModuleImpl;
import com.schibsted.spt.data.jslt.impl.ClasspathResourceResolver;
import com.schibsted.spt.data.jslt.filters.*;

/**
 * Tests that cannot be expressed in JSON.
 */
public class StaticTests extends TestBase {
  private static ObjectMapper mapper = new ObjectMapper();

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
  public void testJavaExtensionFunctionNull() {
    check("{}", "test()", "null", Collections.EMPTY_MAP,
          Collections.singleton(new TestNullFunction()));
  }

  @Test
  public void testJavaExtensionFunctionNullInExpression() {
    check("{}", "test() or 42", "true", Collections.EMPTY_MAP,
          Collections.singleton(new TestNullFunction()));
  }

  @Test
  public void testJavaExtensionFunctionNullInExpression2() {
    check("{}", "lowercase(test())", "null", Collections.EMPTY_MAP,
          Collections.singleton(new TestNullFunction()));
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

  @Test
  public void testIsDecimalFunction() {
    // check that is-decimal still works even if input
    // is a FloatNode and not a DoubleNode
    Expression expr = Parser.compileString("is-decimal(.)");

    JsonNode context = new FloatNode(1.0f);
    JsonNode actual = expr.apply(context);

    assertTrue(actual.isBoolean());
    assertTrue(actual.booleanValue());
  }

  @Test
  public void testIsIntegerFunction() {
    // check that is-integer still works if input
    // is a BigIntegerNode not just IntNode
    Expression expr = Parser.compileString("is-integer(.)");

    JsonNode context = new BigIntegerNode(BigInteger.ONE);
    JsonNode actual = expr.apply(context);

    assertTrue(actual.isBoolean());
    assertTrue(actual.booleanValue());
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

  @Test
  public void testNamedModule() {
    Map<String, Function> functions = new HashMap();
    functions.put("test", new TestFunction());
    ModuleImpl module = new ModuleImpl(functions);

    Map<String, Module> modules = new HashMap();
    modules.put("the test module", module);

    StringReader jslt = new StringReader(
      "import \"the test module\" as t t:test()"
    );
    Expression expr = new Parser(jslt)
      .withNamedModules(modules)
      .compile();

    JsonNode result = expr.apply(null);
    assertEquals(new IntNode(42), result);
  }

  @Test
  public void testJsltObjectFilter() throws IOException {
    // filter to accept everything that isn't null
    String filter = " . != null ";

    StringReader jslt = new StringReader(
      "{ \"foo\" : null, \"bar\" : \"\" }"
    );
    Expression expr = new Parser(jslt)
      .withObjectFilter(filter)
      .compile();

    JsonNode desired = mapper.readTree(
      "{ \"bar\" : \"\" }"
    );

    JsonNode result = expr.apply(null);
    assertEquals(desired, result);
  }

  @Test
  public void testJsltObjectFilter2() throws IOException {
    // filter to accept everything that isn't the empty string
    String filter = " . != \"\" ";

    StringReader jslt = new StringReader(
      "{ \"foo\" : null, \"bar\" : \"\" }"
    );
    Expression expr = new Parser(jslt)
      .withObjectFilter(filter)
      .compile();

    JsonNode desired = mapper.readTree(
      "{ \"foo\" : null }"
    );

    JsonNode result = expr.apply(null);
    assertEquals(desired, result);
  }

  @Test
  public void testJsltObjectFilter3() throws IOException {
    // filter to accept everything that isn't the empty string
    String filter = " . != \"\" ";

    StringReader jslt = new StringReader(
      "{for (.) .key : .value }"
    );
    Expression expr = new Parser(jslt)
      .withObjectFilter(filter)
      .compile();

    JsonNode input = mapper.readTree(
      "{ \"foo\" : null, \"bar\" : \"\" }"
    );

    JsonNode desired = mapper.readTree(
      "{ \"foo\" : null }"
    );

    JsonNode result = expr.apply(input);
    assertEquals(desired, result);
  }

  @Test
  public void testTrueObjectFilter() throws IOException {
    StringReader jslt = new StringReader(
      "{for (.) .key : .value }"
    );
    Expression expr = new Parser(jslt)
      .withObjectFilter(new TrueJsonFilter())
      .compile();

    JsonNode input = mapper.readTree(
      "{ \"foo\" : null, \"bar\" : \"\" }"
    );

    JsonNode desired = mapper.readTree(
      "{ \"foo\" : null, \"bar\" : \"\" }"
    );

    JsonNode result = expr.apply(input);
    assertEquals(desired, result);
  }

  @Test
  public void testTrailingCommasInObject() {
    Expression expr = Parser.compileString("{\"a\":1, \"b\":2,}");
    JsonNode actual = expr.apply(null);

    Iterator<String> it = actual.fieldNames();
    assertEquals("a", it.next());
    assertEquals("b", it.next());
  }

  @Test
  public void testTrailingCommasInArray() {
    Expression expr = Parser.compileString("[1,2,]");
    ArrayNode actual = (ArrayNode) expr.apply(null);

    assertEquals(2, actual.size());

    assertEquals(1, actual.get(0).asInt());
    assertEquals(2, actual.get(1).asInt());
  }

  @Test
  public void testClasspathResolverCharEncoding() {
    ClasspathResourceResolver r = new ClasspathResourceResolver(StandardCharsets.ISO_8859_1);
    Expression expr = new Parser(r.resolve("character-encoding-master.jslt"))
      .withResourceResolver(r)
      .compile();

    JsonNode result = expr.apply(NullNode.instance);
    assertEquals("Hei på deg", result.asText());
  }

  @Test
  public void testPipeOperatorAndObjectMatcher()  throws IOException {
    Expression expr = Parser.compileString("{\"bar\": \"baz\",\"foo\":{ \"a\": \"b\" } | {\"type\" : \"Anonymized-View\",* : .}}");


    JsonNode desired = mapper.readTree(
            "{\"bar\":\"baz\",\"foo\":{\"type\":\"Anonymized-View\",\"a\":\"b\"}}"
    );

    JsonNode result = expr.apply(null);
    assertEquals(desired, result);
  }

  @Test
  public void testTestFunctionCompileFail()  throws IOException {
    // we want to verify that this function fails at compile-time
    // not at runtime
    try {
      Parser.compileString("test(., \"\\\\\")");
      fail("Accepted static, invalid regular expression");
    } catch (JsltException e) {
      assertTrue(e.getMessage().indexOf("regular expression") != -1);
    }
  }

  @Test
  public void testCaptureFunctionCompileFail()  throws IOException {
    // we want to verify that this function fails at compile-time
    // not at runtime
    try {
      Parser.compileString("capture(., \"\\\\\")");
      fail("Accepted static, invalid regular expression");
    } catch (JsltException e) {
      assertTrue(e.getMessage().indexOf("regular expression") != -1);
    }
  }

  @Test
  public void testSplitFunctionCompileFail()  throws IOException {
    // we want to verify that this function fails at compile-time
    // not at runtime
    try {
      Parser.compileString("split(., \"\\\\\")");
      fail("Accepted static, invalid regular expression");
    } catch (JsltException e) {
      assertTrue(e.getMessage().indexOf("regular expression") != -1);
    }
  }

  @Test
  public void testReplaceFunctionCompileFail()  throws IOException {
    // we want to verify that this function fails at compile-time
    // not at runtime
    try {
      Parser.compileString("replace(., \"\\\\\", \"something\")");
      fail("Accepted static, invalid regular expression");
    } catch (JsltException e) {
      assertTrue(e.getMessage().indexOf("regular expression") != -1);
    }
  }
}
