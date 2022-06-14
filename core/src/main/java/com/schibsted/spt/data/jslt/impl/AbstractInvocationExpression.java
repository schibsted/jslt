
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
import java.util.Arrays;
import java.util.ArrayList;
import com.schibsted.spt.data.jslt.Callable;
import com.schibsted.spt.data.jslt.JsltException;

/**
 * Common superclass for function and macro expressions, to avoid
 * having to repeat so much code.
 */
public abstract class AbstractInvocationExpression extends AbstractNode {
  private Callable callable; // null until resolve is called
  protected ExpressionNode[] arguments;

  public AbstractInvocationExpression(ExpressionNode[] arguments,
                                      Location location) {
    super(location);
    this.arguments = arguments;
  }

  // invoked when we know which callable it's going to be
  public void resolve(Callable callable) {
    this.callable = callable;
    if (arguments.length < callable.getMinArguments() ||
        arguments.length > callable.getMaxArguments()) {
      String kind = (this instanceof FunctionExpression) ? "Function" : "Macro";
      throw new JsltException(
        kind + " '" + callable.getName() + "' needs " +
        callable.getMinArguments() + "-" + callable.getMaxArguments() +
        " arguments, got " + arguments.length, location
      );
    }
  }

  public void computeMatchContexts(DotExpression parent) {
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix].computeMatchContexts(parent);
  }

  public ExpressionNode optimize() {
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix] = arguments[ix].optimize();
    return this;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + callable.getName() + "(");
    for (int ix = 0; ix < arguments.length; ix++)
      arguments[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ')');
  }

  public List<ExpressionNode> getChildren() {
    List<ExpressionNode> children = new ArrayList();
    children.addAll(Arrays.asList(arguments));
    return children;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(callable.getName());
    buf.append('(');
    for (int ix = 0; ix < arguments.length; ix++) {
      if (ix > 0)
        buf.append(", ");
      buf.append(arguments[ix].toString());
    }
    buf.append(')');

    return buf.toString();
  }
}
