
package com.schibsted.spt.data.jstl2;

import java.io.File;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jstl2.impl.ExpressionImpl;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;

public class JSTL {

  public static void main(String[] args) throws Exception {
    Expression expr = Parser.compile(new File(args[0]));
    if (expr instanceof ExpressionImpl)
      ((ExpressionImpl) expr).dump();

    ObjectMapper mapper = new ObjectMapper();

    JsonNode input = mapper.readTree(new File(args[1]));

    JsonNode output = expr.apply(input);

    if (output == null)
      System.out.println("WARN: returned Java null!");

    System.out.println(mapper.writerWithDefaultPrettyPrinter()
                       .writeValueAsString(output));
  }

}
