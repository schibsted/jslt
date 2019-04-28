
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

public abstract class AbstractNode implements ExpressionNode {
  protected Location location;

  public AbstractNode(Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + this);
  }

  public void computeMatchContexts(DotExpression parent) {
  }

  public void prepare(PreparationContext ctx) {
    for (ExpressionNode child : getChildren())
      child.prepare(ctx);
  }

  public ExpressionNode optimize() {
    return this;
  }

  public List<ExpressionNode> getChildren() {
    return Collections.EMPTY_LIST;
  }
}
