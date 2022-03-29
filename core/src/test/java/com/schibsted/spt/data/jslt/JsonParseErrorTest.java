
package com.schibsted.spt.data.jslt;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * JSON parsing test cases that are supposed to cause syntax error.
 */
@RunWith(Parameterized.class)
public class JsonParseErrorTest {
  private String json;

  public JsonParseErrorTest(String json) {
    this.json = json;
  }

  @Test
  public void check() {
    try {
      Parser.compileString(json);
      fail("Successfully parsed " + json);
    } catch (JsltException e) {
      // this is what we want
    }
  }

  @Parameters
  public static Collection<Object[]> data() {
    JsonNode json = TestUtils.loadFile("json-parse-error-tests.json");
    JsonNode tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++)
      strings.add(new Object[] { tests.get(ix).asText() });
    return strings;
  }
}
