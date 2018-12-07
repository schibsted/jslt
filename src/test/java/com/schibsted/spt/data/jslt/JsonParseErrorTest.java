
package com.schibsted.spt.data.jslt;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
