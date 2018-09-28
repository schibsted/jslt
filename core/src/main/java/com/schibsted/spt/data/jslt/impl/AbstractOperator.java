
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

/**
 * Shared abstract superclass for comparison operators and others.
 */
public abstract class AbstractOperator extends AbstractNode {
  protected ExpressionNode left;
  protected ExpressionNode right;
  private String operator;

  public AbstractOperator(ExpressionNode left, ExpressionNode right,
                          String operator, Location location) {
    super(location);
    this.left = left;
    this.right = right;
    this.operator = operator;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode v1 = left.apply(scope, input);
    JsonNode v2 = right.apply(scope, input);
    return perform(v1, v2);
  }

  public void dump(int level) {
    left.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + operator);
    right.dump(level + 1);
  }

  public ExpressionNode optimize() {
    left = left.optimize();
    right = right.optimize();
    return this;
  }

  public abstract JsonNode perform(JsonNode v1, JsonNode v2);

  public String toString() {
    return left.toString() + " " + operator + " " + right;
  }
}
