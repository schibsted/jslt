
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class ArrayExpression extends AbstractNode {
  private ExpressionNode[] children;

  public ArrayExpression(ExpressionNode[] children) {
    this.children = children;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    ArrayNode array = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < children.length; ix++)
      array.add(children[ix].apply(scope, input));
    return array;
  }

  public void compile(Compiler compiler) {
    compiler.genPUSHA();
    for (int ix = 0; ix < children.length; ix++) {
      children[ix].compile(compiler);
      compiler.genSETA();
    }
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + '[');
    for (int ix = 0; ix < children.length; ix++)
      children[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ']');
  }
}
