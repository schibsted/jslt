
package com.schibsted.spt.data.jslt.impl;

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

  public ExpressionNode optimize() {
    return this;
  }
}
