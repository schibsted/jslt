
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public class IfExpression extends AbstractNode {
  private ExpressionNode test;
  private LetExpression[] thenlets;
  private ExpressionNode then;
  private LetExpression[] elselets;
  private ExpressionNode orelse;

  public IfExpression(ExpressionNode test,
                      LetExpression[] thenlets,
                      ExpressionNode then,
                      LetExpression[] elselets,
                      ExpressionNode orelse,
                      Location location) {
    super(location);
    this.test = test;
    this.thenlets = thenlets;
    this.then = then;
    this.elselets = elselets;
    this.orelse = orelse;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    if (NodeUtils.isTrue(test.apply(scope, input)))
      return then.apply(NodeUtils.evalLets(scope, input, thenlets), input);

    // test was false, so return null or else
    if (orelse != null)
      return orelse.apply(NodeUtils.evalLets(scope, input, elselets), input);
    else
      return NullNode.instance;
  }

  public void computeMatchContexts(DotExpression parent) {
    for (int ix = 0; ix < thenlets.length; ix++)
      thenlets[ix].computeMatchContexts(parent);
    then.computeMatchContexts(parent);
    if (orelse != null) {
      orelse.computeMatchContexts(parent);
      for (int ix = 0; ix < elselets.length; ix++)
        elselets[ix].computeMatchContexts(parent);
    }
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + "if (");
    test.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ")");

    for (int ix = 0; ix < thenlets.length; ix++)
      thenlets[ix].dump(level + 1);
    then.dump(level + 1);

    if (orelse != null) {
      System.out.println(NodeUtils.indent(level) + "else");
      for (int ix = 0; ix < elselets.length; ix++)
        elselets[ix].dump(level + 1);
      orelse.dump(level + 1);
    }
  }
}
