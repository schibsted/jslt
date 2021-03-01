
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
import java.util.ArrayList;
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

    // if the two operands are literals we can just evaluate the
    // result right now and be done with it
    if (left instanceof LiteralExpression && right instanceof LiteralExpression)
      return new LiteralExpression(apply(null, null), location);
    else
      return this;
  }

  public void computeMatchContexts(DotExpression parent) {
    // operators are transparent to the object matcher
    left.computeMatchContexts(parent);
    right.computeMatchContexts(parent);
  }

  public List<ExpressionNode> getChildren() {
    List<ExpressionNode> children = new ArrayList(2);
    children.add(left);
    children.add(right);
    return children;
  }

  public abstract JsonNode perform(JsonNode v1, JsonNode v2);

  public String toString() {
    String first = (left instanceof AbstractOperator) ? "(" + left + ")" : left.toString();
    String second = (right instanceof AbstractOperator) ? "(" + right + ")" : right.toString();
    return first + " " + operator + " " + second;
  }

  public ExpressionNode getLeft() {
    return left;
  }

  public ExpressionNode getRight() {
    return right;
  }

  public String getOperator() {
    return operator;
  }
}
