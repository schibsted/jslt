
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
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jslt.JsltException;

public class PlusOperator extends NumericOperator {

  public PlusOperator(ExpressionNode left, ExpressionNode right,
                      Location location) {
    super(left, right, "+", location);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    if (v1.isTextual() || v2.isTextual()) {
      // if one operand is string: do string concatenation
      return new TextNode(NodeUtils.toString(v1, false) +
                          NodeUtils.toString(v2, false));

    } else if (v1.isArray() && v2.isArray())
      // if both are arrays: array concatenation
      return concatenateArrays(v1, v2);

    else if (v1.isObject() && v2.isObject())
      // if both are objects: object union
      return unionObjects(v1, v2);

    else
      // do numeric operation
      return super.perform(v1, v2);
  }

  protected double perform(double v1, double v2) {
    return v1 + v2;
  }

  protected long perform(long v1, long v2) {
    return v1 + v2;
  }

  private ArrayNode concatenateArrays(JsonNode v1, JsonNode v2) {
    // .addAll is faster than many .add() calls
    ArrayNode result = NodeUtils.mapper.createArrayNode();
    result.addAll((ArrayNode) v1);
    result.addAll((ArrayNode) v2);
    return result;
  }

  private ObjectNode unionObjects(JsonNode v1, JsonNode v2) {
    // .putAll is faster than many .set() calls
    ObjectNode result = NodeUtils.mapper.createObjectNode();
    result.putAll((ObjectNode) v2);
    result.putAll((ObjectNode) v1); // v1 should overwrite v2
    return result;
  }
}
