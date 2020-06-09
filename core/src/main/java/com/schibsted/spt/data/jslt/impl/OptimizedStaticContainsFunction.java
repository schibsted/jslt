
// Copyright 2019 Schibsted Marketplaces Products & Technology As
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

import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

/**
 * An optimized version of contains(a, b) which is used when b is an
 * array literal with a large number of values, so that a linear
 * search becomes a performance drag.
 */
public class OptimizedStaticContainsFunction extends AbstractFunction {
  private Set<JsonNode> values;

  public OptimizedStaticContainsFunction(JsonNode array) {
    super("optimized-static-contains", 2, 2);

    this.values = new HashSet();
    for (int ix = 0; ix < array.size(); ix++)
      values.add(array.get(ix));
  }

  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    if (values.contains(arguments[0]))
      return BooleanNode.TRUE;
    else
      return BooleanNode.FALSE;
  }
}
