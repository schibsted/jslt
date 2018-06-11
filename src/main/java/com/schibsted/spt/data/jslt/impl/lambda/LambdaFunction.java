
package com.schibsted.spt.data.jslt.impl.lambda;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.impl.NodeUtils;

/**
  * A lambda function used to create the online demo playground via
  * API gateway.
  */
public class LambdaFunction {

  /**
   * Transform the incoming JSON with JSLT and return the result.
   */
  public String invoke(String json) {
    try {
      // this must be:
      // {"json" : ..., "jslt" : jslt}
      JsonNode input = NodeUtils.mapper.readTree(json);

      // now we can do the thing
      JsonNode source = NodeUtils.mapper.readTree(input.get("json").asText());
      String jslt = input.get("jslt").asText();

      Expression template = Parser.compileString(jslt);
      JsonNode output = template.apply(source);
      return NodeUtils.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
    } catch (Throwable e) {
      return "ERROR: " + e;
    }
  }
}
