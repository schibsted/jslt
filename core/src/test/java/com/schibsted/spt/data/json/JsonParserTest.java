
package com.schibsted.spt.data.json;

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
    error("truu");
    error("tru");
    error("truee");
    error("fasle");
  }

  @Test
  public void testString() {
    assertEquals(new StringJValue(""), load("\"\""));
    assertEquals(new StringJValue("foo"), load("\"foo\""));
  }

  @Test
  public void testIntegers() {
    assertEquals(new LongJValue(42), load("42"));
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
