
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;

public class FunctionExpression extends AbstractInvocationExpression {
  private Function function; // null before resolution
  private FunctionDeclaration declared; // non-null if a declared function
  private String name;

  public FunctionExpression(String name, ExpressionNode[] arguments,
                            Location location) {
    super(arguments, location);
    this.name = name;
  }

  public String getFunctionName() {
    return name;
  }

  public void resolve(Function function) {
    super.resolve(function);
    this.function = function;
    if (function instanceof FunctionDeclaration)
      this.declared = (FunctionDeclaration) function;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode[] params = new JsonNode[arguments.length];
    for (int ix = 0; ix < params.length; ix++)
      params[ix] = arguments[ix].apply(scope, input);

    if (declared != null)
      return declared.call(scope, input, params);
    else
      return function.call(input, params);
  }
}
