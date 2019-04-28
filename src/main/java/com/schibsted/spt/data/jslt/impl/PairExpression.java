
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
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a ("key" : expr) pair inside a JSON object.
 */
public class PairExpression extends AbstractNode {
  private String key;
  private ExpressionNode expr;

  public PairExpression(String key, ExpressionNode expr, Location location) {
    super(location);
    this.key = key;
    this.expr = expr;
  }

  public String getKey() {
    return key;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return expr.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    expr.computeMatchContexts(new DotExpression(key, parent, location));
  }

  // is the expr a literal?
  public boolean isLiteral() {
    return expr instanceof LiteralExpression;
  }

  public ExpressionNode optimize() {
    expr = expr.optimize();
    return this;
  }

  public List<ExpressionNode> getChildren() {
    return Collections.singletonList(expr);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + '"' + key + '"' + " :");
    expr.dump(level + 1);
  }
}
