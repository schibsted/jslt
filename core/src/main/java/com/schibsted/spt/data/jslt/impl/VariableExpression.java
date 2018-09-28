
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
import com.schibsted.spt.data.jslt.JsltException;

public class VariableExpression extends AbstractNode {
  private String variable;

  public VariableExpression(String variable, Location location) {
    super(location);
    this.variable = variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode value = scope.getValue(variable);
    if (value == null)
      throw new JsltException("No such variable '" + variable + "'",
                              location);
    return value;
  }

  public void dump(int level) {
  }

  public String toString() {
    return "$" + variable;
  }
}
