
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.schibsted.spt.data.jstl2.JstlException;

public class ArraySlicer implements ExpressionNode {
  private ExpressionNode slicer;
  private ExpressionNode parent;

  public ArraySlicer(ExpressionNode slicer, ExpressionNode parent) {
    this.slicer = slicer;
    this.parent = parent;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode array = parent.apply(scope, input);
    if (!array.isArray())
      return NullNode.instance;

    JsonNode slice = NodeUtils.number(slicer.apply(scope, input));
    if (!slice.isNumber())
      throw new JstlException("Can't index array with " + slice);

    int index = slice.intValue();
    if (index < 0)
      index = array.size() + index;
    return array.get(index);
  }

  public void dump(int level) {
    if (parent != null)
      parent.dump(level);
    System.out.println(NodeUtils.indent(level) + this);
  }

  public String toString() {
    return "[" + slicer + "]";
  }
}
