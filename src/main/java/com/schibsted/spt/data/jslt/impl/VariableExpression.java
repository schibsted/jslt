
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
  private int slot;
  private VariableInfo info;

  public VariableExpression(String variable, Location location) {
    super(location);
    this.variable = variable;
    this.slot = ScopeManager.UNFOUND;
  }

  public String getVariable() {
    return variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode value = scope.getValue(slot);
    if (value == null)
      throw new JsltException("No such variable '" + variable + "'",
                              location);
    return value;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + this);
  }

  public void prepare(PreparationContext ctx) {
    info = ctx.scope.resolveVariable(this);
    slot = info.getSlot();
    info.incrementUsageCount();
  }

  public ExpressionNode optimize() {
    // if the variable is assigned to a literal then there's no point
    // in actually having a variable. we can just insert the literal
    // in the expression tree and be done with it.
    ExpressionNode declaration = info.getDeclaration();
    // will be null if the variable is a parameter
    if (declaration != null && (declaration instanceof LiteralExpression))
      return declaration;
    else
      return this;
  }

  public String toString() {
    return "$" + variable;
  }
}
