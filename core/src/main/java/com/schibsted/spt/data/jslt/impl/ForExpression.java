
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
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jslt.JsltException;

public class ForExpression extends AbstractNode {
  private ExpressionNode valueExpr;
  private LetExpression[] lets;
  private ExpressionNode loopExpr;
  private ExpressionNode ifExpr;

  public ForExpression(ExpressionNode valueExpr,
                       LetExpression[] lets,
                       ExpressionNode loopExpr,
                       ExpressionNode ifExpr,
                       Location location) {
    super(location);
    this.valueExpr = valueExpr;
    this.lets = lets;
    this.loopExpr = loopExpr;
    this.ifExpr = ifExpr;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode array = valueExpr.apply(scope, input);
    if (array.isNull())
      return NullNode.instance;
    else if (array.isObject())
      array = NodeUtils.convertObjectToArray(array);
    else if (!array.isArray())
      throw new JsltException("For loop can't iterate over " + array, location);

    ArrayNode result = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < array.size(); ix++) {
      JsonNode value = array.get(ix);

      // must evaluate lets over again for each value because of context
      if (lets.length > 0)
        NodeUtils.evalLets(scope, value, lets);

      if (ifExpr == null || NodeUtils.isTrue(ifExpr.apply(scope, value)))
        result.add(loopExpr.apply(scope, value));
    }
    return result;
  }

  public void computeMatchContexts(DotExpression parent) {
    // if you do matching inside a 'for' the matching is on the
    // current object being traversed in the list. so we forget the
    // parent and start over
    loopExpr.computeMatchContexts(new DotExpression(location));
  }

  public ExpressionNode optimize() {
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].optimize();

    valueExpr = valueExpr.optimize();
    loopExpr = loopExpr.optimize();
    if (ifExpr != null)
      ifExpr = ifExpr.optimize();
    return this;
  }

  public void prepare(PreparationContext ctx) {
    ctx.scope.enterScope();

    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].register(ctx.scope);

    for (ExpressionNode child : getChildren())
      child.prepare(ctx);

    ctx.scope.leaveScope();
  }

  public List<ExpressionNode> getChildren() {
    List<ExpressionNode> children = new ArrayList();
    children.addAll(Arrays.asList(lets));
    children.add(valueExpr);
    children.add(loopExpr);
    if (ifExpr != null)
      children.add(ifExpr);
    return children;
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + "for (");
    valueExpr.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ")");
    loopExpr.dump(level + 1);
  }

  public String toString() {
    return "[for (" + valueExpr + ") " + loopExpr +
      ((ifExpr != null) ? (" if(" + ifExpr + ")") : "") +
      "]";
  }
}
