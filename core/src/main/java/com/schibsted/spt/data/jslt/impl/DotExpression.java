
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
import com.fasterxml.jackson.databind.node.NullNode;

public class DotExpression extends AbstractNode {
  private String key;
  private ExpressionNode parent;

  public DotExpression(Location location) {
    super(location);
  }

  public DotExpression(String key, ExpressionNode parent, Location location) {
    super(location);
    this.key = key;
    this.parent = parent;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    // if there is no key we just return the input
    if (key == null)
      return input;

    // if we have a parent, get the input from the parent (preceding expr)
    if (parent != null)
      input = parent.apply(scope, input);

    // okay, do the keying
    JsonNode value = input.get(key);
    if (value == null)
      value = NullNode.instance;
    return value;
  }

  public List<ExpressionNode> getChildren() {
    if (parent == null)
      return Collections.EMPTY_LIST;
    else
      return Collections.singletonList(parent);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + this);
  }

  public String toString() {
    String me = "." + (key == null ? "" : key);
    if (parent != null)
      return "" + parent + me;
    else
      return me;
  }

  // verify that we've build a correct DotExpression for our object
  // matcher (only used for that)
  public void checkOk(Location matcher) {
    // this object is OK, but might be a FailDotExpression higher up,
    // so check for that
    if (parent != null)
      ((DotExpression) parent).checkOk(matcher);
  }

  public ExpressionNode optimize() {
    if (parent != null)
      parent = parent.optimize();
    return this;
  }
}
