
package com.schibsted.spt.data.jstl2.impl;

import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public abstract class AbstractNode implements ExpressionNode {
  protected Location location;

  public AbstractNode(Location location) {
    this.location = location;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + this);
  }

  public void computeMatchContexts(DotExpression parent) {
  }

  public void compile(Compiler compiler) {
  }

  public ExpressionNode optimize() {
    return this;
  }
}
