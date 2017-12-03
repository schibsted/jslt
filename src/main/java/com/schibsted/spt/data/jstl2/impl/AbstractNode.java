
package com.schibsted.spt.data.jstl2.impl;

public abstract class AbstractNode implements ExpressionNode {

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + this);
  }

  public void computeMatchContexts(DotExpression parent) {
  }

}
