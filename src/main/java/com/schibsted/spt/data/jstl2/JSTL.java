
package com.schibsted.spt.data.jstl2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSTL {

  public static void main(String[] args) throws Exception {
    Expression expr = Parser.compile(args[0]);
    ObjectMapper mapper = new ObjectMapper();

    JsonNode input = mapper.readTree(args[1]);

    JsonNode output = expr.apply(input);

    mapper.writerWithDefaultPrettyPrinter()
      .writeValueAsString(output);
  }

}
