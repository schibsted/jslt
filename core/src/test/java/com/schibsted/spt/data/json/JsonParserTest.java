
package com.schibsted.spt.data.json;

import java.math.BigInteger;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.schibsted.spt.data.jslt.JsltException;

public class JsonParserTest {

  @Test
  public void testNothing() {
    error("");
    error("   ");
  }

  @Test
  public void testNull() {
    assertEquals(NullJValue.instance, load("null"));
  }

  @Test
  public void testBoolean() {
    assertEquals(BooleanJValue.TRUE, load("true"));
    assertEquals(BooleanJValue.FALSE, load("false"));
    assertEquals(BooleanJValue.TRUE, load("   true"));
    assertEquals(BooleanJValue.FALSE, load("  false"));
    assertEquals(BooleanJValue.TRUE, load("true "));
    error("truu");
    error("tru");
    error("truee");
    error("fasle");
  }

  @Test
  public void testString() {
    assertEquals(new StringJValue(""), load("\"\""));
    assertEquals(new StringJValue("foo"), load("\"foo\""));
    assertEquals(new StringJValue("foo \" bar"), load("\"foo \\\" bar\""));
    assertEquals(new StringJValue("foo \\ bar"), load("\"foo \\\\ bar\""));
  }

  @Test
  public void testIntegers() {
    assertEquals(new LongJValue(42), load("42"));
    assertEquals(new LongJValue(-42), load("-42"));
  }

  @Test
  public void testDecimals() {
    assertEquals(new DoubleJValue(3.14), load("3.14"));
    assertEquals(new DoubleJValue(-3.14), load("-3.14"));

    JsonValue value = load("-4.4E-4");
    assertEquals(-0.00044, value.asDouble(), 0.00001);
  }

  @Test
  public void testObject() {
    assertEquals(new DynamicJObject(), load("{}"));

    DynamicJObject single = new DynamicJObject();
    single.set("foo", new StringJValue("bar"));
    assertEquals(single, load("{\"foo\" : \"bar\"}"));

    DynamicJObject two = new DynamicJObject();
    two.set("foo", new LongJValue(2732));
    two.set("baz", new StringJValue("bar"));
    assertEquals(two, load("{\"foo\" : 2732, \"baz\":\"bar\" }"));
  }

  @Test
  public void testArray() {
    assertEquals(new FixedArrayJValue(new JsonValue[0], 0), load("[]"));

    FixedArrayJValue single = new FixedArrayJValue(new JsonValue[]{
      new LongJValue(-2313)
    }, 1);
    assertEquals(single, load("[-2313]"));

    FixedArrayJValue two = new FixedArrayJValue(new JsonValue[]{
      new LongJValue(2732),
      new StringJValue("bar")
    }, 2);
    assertEquals(two, load("[2732, \"bar\"]"));

    JsonValue[] array = new JsonValue[12];
    for (int ix = 0; ix < array.length; ix++)
      array[ix] = new LongJValue(ix);
    FixedArrayJValue big = new FixedArrayJValue(array, array.length);
    assertEquals(big, load(big.toString()));
  }

  @Test
  public void testBigInteger() {
    BigIntegerJValue v = new BigIntegerJValue(new BigInteger("124274772478237237823782728372873000000012347427427378238238283"));
    JsonValue v2 = load(v.toString());
    assertEquals(v, v2);
  }

  @Test
  public void testByteParsing() throws IOException {
    byte[] bytes = "[1,2,3]".getBytes();
    JsonValue v = JsonIO.parse(bytes);
    JsonValue v2 = load("[1,2,3]");
    assertEquals(v, v2);
  }

  // ===== UTILITIES

  private void error(String json) {
    try {
      load(json);
      fail("Was able to load '" + json + "' without error");
    } catch (JsltException e) {
      // expected
    }
  }

  private JsonValue load(String json) {
    return JsonIO.parseString(json);
  }
}
