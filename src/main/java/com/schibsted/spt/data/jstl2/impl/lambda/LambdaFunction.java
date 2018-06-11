
package com.schibsted.spt.data.jstl2.impl.lambda;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jstl2.Expression;
import com.schibsted.spt.data.jstl2.impl.NodeUtils;

/**
  * A lambda function used to create a JSTL 2.0 online demo playground
  * via API gateway.
  */
public class LambdaFunction {

  /**
   * Transform the incoming JSON with JSTL and return the result.
   */
  public String invoke(String json) {
    try {
      // this must be:
      // {"json" : ..., "jstl" : jstl}
      JsonNode input = NodeUtils.mapper.readTree(json);

      // now we can do the thing
      JsonNode source = NodeUtils.mapper.readTree(input.get("json").asText());
      String jstl = input.get("jstl").asText();

      Expression template = Parser.compile(jstl);
      JsonNode output = template.apply(source);
      return NodeUtils.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
    } catch (Throwable e) {
      return "ERROR: " + e;
    }
  }
}
