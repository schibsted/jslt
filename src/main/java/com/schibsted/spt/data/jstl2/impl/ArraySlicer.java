
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class ArraySlicer extends AbstractNode {
  private ExpressionNode left;
  private boolean colon;
  private ExpressionNode right;
  private ExpressionNode parent;

  public ArraySlicer(ExpressionNode left, boolean colon, ExpressionNode right,
                     ExpressionNode parent) {
    this.left = left;
    this.colon = colon;
    this.right = right;
    this.parent = parent;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode array = parent.apply(scope, input);
    if (!array.isArray())
      return NullNode.instance;

    int leftix = resolveIndex(scope, left, input, array, 0);
    if (!colon)
      return array.get(leftix);

    int rightix = resolveIndex(scope, right, input, array, array.size());
    if (rightix > array.size())
      rightix = array.size();

    ArrayNode result = NodeUtils.mapper.createArrayNode();
    for (int ix = leftix; ix < rightix; ix++)
      result.add(array.get(ix));
    return result;
  }

  private int resolveIndex(Scope scope, ExpressionNode expr,
                           JsonNode input, JsonNode array, int ifnull) {
    if (expr == null)
      return ifnull;

    JsonNode node = expr.apply(scope, input);
    if (!node.isNumber())
      throw new JstlException("Can't index array with " + node);

    int ix = node.intValue();
    if (ix < 0)
      ix = array.size() + ix;
    return ix;
  }

  public void compile(Compiler compiler) {
    parent.compile(compiler);
    if (left == null)
      compiler.genPUSHL(new IntNode(0));
    else
      left.compile(compiler);

    if (!colon)
      compiler.genAIDX();
    else {
      if (right == null)
        compiler.genPUSHL(NullNode.instance);
      else
        right.compile(compiler);
      compiler.genASLC();
    }
  }

  public void dump(int level) {
    if (parent != null)
      parent.dump(level);
    System.out.println(NodeUtils.indent(level) + this);
  }

  public String toString() {
    return "[" + left + " : " + right + "]";
  }
}
