
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class LetExpression extends AbstractNode {
  private String variable;
  private ExpressionNode value;
  private int slot; // this variable's position in the stack frame
  private VariableInfo info;

  public LetExpression(String variable, ExpressionNode value, Location location) {
    super(location);
    this.variable = variable;
    this.value = value;
    this.slot = ScopeManager.UNFOUND;
  }

  public String getVariable() {
    return variable;
  }

  public int getSlot() {
    return slot;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    value.computeMatchContexts(parent);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) +
                       "let " + variable + " =");
    value.dump(level + 1);
  }

  public List<ExpressionNode> getChildren() {
    return Collections.singletonList(value);
  }

  public ExpressionNode optimize() {
    value = value.optimize();
    return this;
  }

  public void register(ScopeManager scope) {
    info = scope.registerVariable(this);
    slot = info.getSlot();
  }

  public ExpressionNode getDeclaration() {
    return value;
  }
}
