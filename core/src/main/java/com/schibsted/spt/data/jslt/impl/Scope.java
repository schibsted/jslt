
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
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;

public class Scope {
  private static Scope root = new Scope(Collections.EMPTY_MAP, null);

  public static Scope getRoot() {
    return root;
  }

  public static Scope makeScope(Map<String, JsonNode> variables) {
    return new Scope(variables, null);
  }

  public static Scope makeScope(Map<String, JsonNode> variables, Scope parent) {
    return new Scope(variables, parent);
  }

  private Scope parent;
  private Map<String, JsonNode> variables;

  private Scope(Map<String, JsonNode> variables, Scope parent) {
    this.parent = parent;
    this.variables = variables;
  }

  public JsonNode getValue(String variable) {
    JsonNode value = variables.get(variable);
    if (value == null && parent != null)
      value = parent.getValue(variable);
    return value;
  }
}
