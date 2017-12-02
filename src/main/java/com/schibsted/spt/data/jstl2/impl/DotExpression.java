
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

public class DotExpression implements ExpressionNode {
  private String key;
  private ExpressionNode parent;

  public DotExpression() {
  }

  public DotExpression(String key, ExpressionNode parent) {
    this.key = key;
    this.parent = parent;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    // if there is no key we just return the input
    if (key == null)
      return input; // FIXME: should we make a copy?

    // if we have a parent, get the input from the parent (preceding expr)
    if (parent != null)
      input = parent.apply(scope, input);

    // okay, do the keying
    JsonNode value = input.get(key);
    if (value == null)
      value = NullNode.instance;
    return value;
  }

  public void dump(int level) {
  }
}
