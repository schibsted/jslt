
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
      String jslt = input.get("jstl").asText();

      Expression template = Parser.compileString(jslt);
      JsonNode output = template.apply(source);
      return NodeUtils.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
    } catch (Throwable e) {
      return "ERROR: " + e;
    }
  }
}
