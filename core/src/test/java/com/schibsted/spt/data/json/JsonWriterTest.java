
package com.schibsted.spt.data.json;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.schibsted.spt.data.jslt.TestUtils;

/**
 * JSON serialization tests.
 */
@RunWith(Parameterized.class)
public class JsonWriterTest {
  private String json;
  private static JsonWriter writer = new JsonWriter();

  public JsonWriterTest(String json) {
    this.json = json;
  }

  @Test
  public void check() {
    byte[] tmp = null;
    try {
      JsonValue v = JsonIO.parseString(json);
      tmp = writer.toBytes(v);
      JsonValue v2 = JsonIO.parse(tmp);

      assertTrue("bad serialization: " + s(tmp), v.equals(v2));
    } catch (IOException e) {
      throw new RuntimeException("Parsing '" + s(tmp) + "' failed", e);
    }
  }

  private static String s(byte[] tmp) {
    return new String(tmp, 0, tmp.length);
  }

  @Parameters(name= "input: {0}")
  public static Collection<Object[]> data() {
    JsonValue json = TestUtils.loadFile("json-parse-tests.json");
    JsonValue tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++)
      strings.add(new Object[] { tests.get(ix).asString() });
    return strings;
  }
}
