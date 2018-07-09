
package com.schibsted.spt.data.jslt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.io.IOException;

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
      fail("JSTL did not detect error");
    } catch (JsltException e) {
      assertTrue("incorrect error message: '" + e.getMessage() + "'",
                 e.getMessage().indexOf(error) != -1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Parameters
  public static Collection<Object[]> data() {
    JsonNode json = TestUtils.loadJson("query-error-tests.json");
    JsonNode tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++) {
      JsonNode test = tests.get(ix);
      strings.add(new Object[] {
          test.get("input").asText(),
          test.get("query").asText(),
          test.get("error").asText()
        });
    }
    return strings;
  }
}
