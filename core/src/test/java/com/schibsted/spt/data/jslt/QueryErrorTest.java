
package com.schibsted.spt.data.jslt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Checks that JSLT queries produce certain runtime errors.
 */
@RunWith(Parameterized.class)
public class QueryErrorTest extends TestBase {

  private static ObjectMapper mapper = new ObjectMapper();
  private String input;
  private String query;
  private String error;

  public QueryErrorTest(String input, String query, String error) {
    this.input = input;
    this.query = query;
    this.error = error;
  }

  @Test
  public void check() {
    try {
      JsonNode context = mapper.readTree(input);

      Expression expr = Parser.compileString(query);
      JsonNode actual = expr.apply(context);
      fail("JSLT did not detect error in " + query);
    } catch (JsltException e) {
      assertTrue("incorrect error message: '" + e.getMessage() + "', " +
                 "correct: '" + error + "'",
                 e.getMessage().indexOf(error) != -1);
    } catch (Exception e) {
      throw new RuntimeException("Failure on query " + query + ": " + e, e);
    }
  }

  @Parameters(name = "query: {1}")
  public static Collection<Object[]> data() {
    List<Object[]> strings = new ArrayList();
    strings.addAll(loadTests("query-error-tests.json"));
    strings.addAll(loadTests("function-error-tests.json"));
    strings.addAll(loadTests("function-declaration-tests.yaml"));
    return strings;
  }

  private static Collection<Object[]> loadTests(String resource) {
    JsonNode json = TestUtils.loadFile(resource);
    JsonNode tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++) {
      JsonNode test = tests.get(ix);
      if (!test.has("error"))
        // not an error test, so skip it
        // this works because we load the same file in QueryTest
        continue;

      strings.add(new Object[] {
          test.get("input").asText(),
          test.get("query").asText(),
          test.get("error").asText()
        });
    }
    return strings;
  }
}
