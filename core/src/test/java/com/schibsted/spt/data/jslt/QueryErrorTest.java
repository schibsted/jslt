
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

import com.schibsted.spt.data.json.*;

/**
 * Checks that JSLT queries produce certain runtime errors.
 */
@RunWith(Parameterized.class)
public class QueryErrorTest extends TestBase {

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
      JsonValue context = JsonIO.parseString(input);

      Expression expr = Parser.compileString(query);
      JsonValue actual = expr.apply(context);
      fail("JSLT did not detect error in " + query);
    } catch (JsltException e) {
      assertTrue("incorrect error message: '" + e.getMessage() + "', " +
                 "correct: '" + error + "'",
                 e.getMessage().indexOf(error) != -1);
    } catch (Exception e) {
      throw new RuntimeException("Failure on query " + query + ": " + e, e);
    }
  }

  @Parameters(name = "query: {1}, input {0}")
  public static Collection<Object[]> data() {
    List<Object[]> strings = new ArrayList();
    strings.addAll(loadTests("query-error-tests.json"));
    strings.addAll(loadTests("function-error-tests.json"));
    strings.addAll(loadTests("function-declaration-tests.yaml"));
    return strings;
  }

  private static Collection<Object[]> loadTests(String resource) {
    JsonValue json = TestUtils.loadFile(resource);
    JsonValue tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++) {
      JsonValue test = tests.get(ix);
      if (!test.has("error"))
        // not an error test, so skip it
        // this works because we load the same file in QueryTest
        continue;

      strings.add(new Object[] {
          test.get("input").asString(),
          test.get("query").asString(),
          test.get("error").asString()
        });
    }
    return strings;
  }
}
