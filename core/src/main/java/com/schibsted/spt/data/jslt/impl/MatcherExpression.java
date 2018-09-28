
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

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents the '* - ... : .'
 */
public class MatcherExpression extends AbstractNode {
  private List<String> minuses;
  private ExpressionNode expr;

  public MatcherExpression(ExpressionNode expr, List<String> minuses,
                           Location location) {
    super(location);
    this.minuses = minuses;
    this.expr = expr;
  }

  public List<String> getMinuses() {
    return minuses;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return expr.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    // FIXME: uhhh, the rules here?
  }

  public void dump(int level) {
  }
}
