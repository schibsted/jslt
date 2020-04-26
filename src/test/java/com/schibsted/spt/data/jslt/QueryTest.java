
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

/**
 * Test cases verifying queries against an input.
 */
@RunWith(Parameterized.class)
public class QueryTest extends TestBase {
  private static ObjectMapper mapper = new ObjectMapper();
  private String input;
  private String query;
  private String output;
  private Map<String, JsonNode> variables;

  public QueryTest(String input, String query, String output,
                   Map<String, JsonNode> variables) {
    this.input = input;
    this.query = query;
    this.output = output;
    this.variables = variables;
  }

  @Test
  public void check() {
    try {
      JsonNode context = mapper.readTree(input);

      Expression expr = Parser.compileString(query);
      JsonNode actual = expr.apply(variables, context);
      if (actual == null)
        throw new JsltException("Returned Java null");

      // reparse to handle IntNode(2) != LongNode(2)
      actual = mapper.readTree(mapper.writeValueAsString(actual));

      JsonNode expected = mapper.readTree(output);

      assertEquals("" + expected + " != " + actual + " in query " + query + ", input: " + input + ", actual class " + actual.getClass() + ", expected class " + expected.getClass(), expected, actual);
    } catch (Exception e) {
      throw new RuntimeException("Failure on query " + query + ": " + e, e);
    }
  }

  @Parameters(name= "query: {1}")
  public static Collection<Object[]> data() {
    List<Object[]> strings = new ArrayList();
    strings.addAll(loadTests("query-tests.json"));
    strings.addAll(loadTests("query-tests.yaml"));
    strings.addAll(loadTests("function-tests.json"));
    strings.addAll(loadTests("experimental-tests.json"));
    strings.addAll(loadTests("function-declaration-tests.yaml"));
    return strings;
  }

  private static Collection<Object[]> loadTests(String resource) {
    JsonNode json = TestUtils.loadFile(resource);
    JsonNode tests = json.get("tests");

    List<Object[]> strings = new ArrayList();
    for (int ix = 0; ix < tests.size(); ix++) {
      JsonNode test = tests.get(ix);
      if (!test.has("output"))
        // not a query test, so skip it
        // this works because we load the same file in QueryErrorTest
        continue;

      strings.add(new Object[] {
          test.get("input").asText(),
          test.get("query").asText(),
          test.get("output").asText(),
          toMap(test.get("variables"))
        });
    }
    return strings;
  }

  private static Map<String, JsonNode> toMap(JsonNode json) {
    Map<String, JsonNode> variables = new HashMap();
    if (json != null) {
      Iterator<String> it = json.fieldNames();
      while (it.hasNext()) {
        String field = it.next();
        variables.put(field, json.get(field));
      }
    }
    return variables;
  }
}
