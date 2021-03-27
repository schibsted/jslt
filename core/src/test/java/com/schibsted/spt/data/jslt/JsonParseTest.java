
package com.schibsted.spt.data.jslt;

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

import com.schibsted.spt.data.json.*;

/**
 * JSON parsing test cases. Verifies that Jackson and JSLT produce the
 * same JSON structure.
 */
@RunWith(Parameterized.class)
public class JsonParseTest {
  private String json;

  public JsonParseTest(String json) {
    this.json = json;
  }

  @Test
  public void check() {
    try {
      Expression expr = Parser.compileString(json);
      JsonValue actual = expr.apply(null);

      JsonValue expected = JsonIO.parseString(json);

      assertEquals("actual class " + actual.getClass() + ", expected class " + expected.getClass(), expected, actual);
    } catch (JsltException e) {
      throw new RuntimeException("Parsing '" + json + "' failed", e);
    }
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
