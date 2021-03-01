
// Copyright 2018 Schibsted Marketplaces Products & Technology As
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.schibsted.spt.data.jslt.cli;

import java.io.File;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.impl.ExpressionImpl;

public class JSLT {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: java com.schibsted.spt.data.jslt.cli.JSLT <jslt file> <json input file>");
      System.exit(1);
    }

    Expression expr = Parser.compile(new File(args[0]));
    // if (expr instanceof ExpressionImpl)
    //   ((ExpressionImpl) expr).dump();

    ObjectMapper mapper = new ObjectMapper();

    JsonNode input = null;
    try {
      input = mapper.readTree(new File(args[1]));
    } catch (JsonParseException e) {
      System.out.println("Couldn't parse JSON file '" + args[1] + "': " + e.getMessage());
      System.exit(1);
    }

    JsonNode output = expr.apply(input);

    if (output == null)
      System.out.println("WARN: returned Java null!");

    System.out.println(mapper.writerWithDefaultPrettyPrinter()
                       .writeValueAsString(output));
  }

}
