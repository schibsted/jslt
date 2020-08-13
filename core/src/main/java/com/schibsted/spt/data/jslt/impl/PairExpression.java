
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
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.JsltException;

/**
 * Represents a ("key" : expr) pair inside a JSON object.
 */
public class PairExpression extends AbstractNode {
  private ExpressionNode key;
  private ExpressionNode value;

  public PairExpression(ExpressionNode key, ExpressionNode value, Location location) {
    super(location);
    this.key = key;
    this.value = value;
  }

  public String applyKey(Scope scope, JsonNode input) {
    JsonNode v = key.apply(scope, input);
    if (!v.isTextual()) {
      throw new JsltException("Object key must be string", location);
    }
    return v.asText();
  }

  public String getStaticKey() {
    if (!isKeyLiteral())
      throw new JsltException("INTERNAL ERROR: Attempted to get non-static key");
    return key.apply(null, null).asText();
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value.apply(scope, input);
  }

  public void computeMatchContexts(DotExpression parent) {
    // a pair that has a dynamic key cannot use matching in the value
    DotExpression expr;
    if (isKeyLiteral())
      expr = new DotExpression(getStaticKey(), parent, location);
    else
      expr = new FailDotExpression(location, "dynamic object");

    value.computeMatchContexts(expr);
  }

  public boolean isLiteral() {
    return value instanceof LiteralExpression && key instanceof LiteralExpression;
  }

  public boolean isKeyLiteral() {
    return key instanceof LiteralExpression;
  }

  public ExpressionNode optimize() {
    key = key.optimize();
    value = value.optimize();
    return this;
  }

  public List<ExpressionNode> getChildren() {
    return Arrays.asList(key, value);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + '"' + key + '"' + " :");
    value.dump(level + 1);
  }
}
