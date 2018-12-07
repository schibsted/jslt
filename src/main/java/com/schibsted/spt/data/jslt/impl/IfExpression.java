
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public class IfExpression extends AbstractNode {
  private ExpressionNode test;
  private LetExpression[] thenlets;
  private ExpressionNode then;
  private LetExpression[] elselets; // can be null
  private ExpressionNode orelse; // can be null

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
    if (NodeUtils.isTrue(test.apply(scope, input))) {
      NodeUtils.evalLets(scope, input, thenlets);
      return then.apply(scope, input);
    }

    // test was false, so return null or else
    if (orelse != null) {
      NodeUtils.evalLets(scope, input, elselets);
      return orelse.apply(scope, input);
    } else
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

  public ExpressionNode optimize() {
    for (int ix = 0; ix < thenlets.length; ix++)
      thenlets[ix].optimize();
    if (elselets != null) {
      for (int ix = 0; ix < elselets.length; ix++)
        elselets[ix].optimize();
    }

    test = test.optimize();
    then = then.optimize();
    if (orelse != null)
      orelse = orelse.optimize();
    return this;
  }


  public void prepare(PreparationContext ctx) {
    test.prepare(ctx);

    // then
    ctx.scope.enterScope();
    for (int ix = 0; ix < thenlets.length; ix++) {
      thenlets[ix].prepare(ctx);
      thenlets[ix].register(ctx.scope);
    }
    then.prepare(ctx);
    ctx.scope.leaveScope();

    // else
    if (orelse != null) {
      ctx.scope.enterScope();
      for (int ix = 0; ix < elselets.length; ix++) {
        elselets[ix].prepare(ctx);
        elselets[ix].register(ctx.scope);
      }
      orelse.prepare(ctx);
      ctx.scope.leaveScope();
    }
  }

  public List<ExpressionNode> getChildren() {
    List<ExpressionNode> children = new ArrayList();
    children.add(test);
    children.addAll(Arrays.asList(thenlets));
    children.add(then);
    if (elselets != null)
      children.addAll(Arrays.asList(elselets));
    if (orelse != null)
      children.add(orelse);
    return children;
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
