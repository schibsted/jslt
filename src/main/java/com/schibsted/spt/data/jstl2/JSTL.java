
package com.schibsted.spt.data.jstl2;

import java.io.File;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSTL {

  public static void main(String[] args) throws Exception {
    Expression expr = Parser.compile(new File(args[0]));
    ObjectMapper mapper = new ObjectMapper();

    JsonNode input = mapper.readTree(new File(args[1]));

    JsonNode output = expr.apply(input);

    System.out.println(mapper.writerWithDefaultPrettyPrinter()
                       .writeValueAsString(output));
  }

}
