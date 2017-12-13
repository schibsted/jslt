
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

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

  public void compile(Compiler compiler) {
    if (parent != null)
      parent.compile(compiler);
    else
      compiler.genPUSHI();

    if (key != null)
      compiler.genDOT(key);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + this);
  }

  public String toString() {
    String me = " ." + (key == null ? "<nothing>" : key);
    if (parent != null)
      return "" + parent + me;
    else
      return me;
  }
}
