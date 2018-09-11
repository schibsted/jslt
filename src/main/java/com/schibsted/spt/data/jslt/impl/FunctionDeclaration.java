
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

package com.schibsted.spt.data.jslt.impl;

import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.Function;

public class FunctionDeclaration implements Function {
  private String name;
  private String[] parameters;
  private LetExpression[] lets;
  private ExpressionNode body;

  public FunctionDeclaration(String name, String[] parameters,
                             LetExpression[] lets, ExpressionNode body) {
    this.name = name;
    this.parameters = parameters;
    this.lets = lets;
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public int getMinArguments() {
    return parameters.length;
  }

  public int getMaxArguments() {
    return parameters.length;
  }

  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    // build scope inside function body, params first
    Map<String, JsonNode> params = new HashMap(arguments.length);
    for (int ix = 0; ix < arguments.length; ix++)
      params.put(parameters[ix], arguments[ix]);
    Scope scope = Scope.makeScope(params);

    // then lets
    if (lets.length > 0)
      scope = NodeUtils.evalLets(scope, input, lets);

    // evaluate body
    return body.apply(scope, input);
  }

  public void optimize() {
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].optimize();

    body = body.optimize();
  }

  public void computeMatchContexts(DotExpression parent) {
    // not allowed to use object matcher inside declared functions
    FailDotExpression fail = new FailDotExpression(null, "function declaration");
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].computeMatchContexts(fail);
    body.computeMatchContexts(fail);
  }
}
