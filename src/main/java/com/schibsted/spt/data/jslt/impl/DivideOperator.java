
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

public class DivideOperator extends NumericOperator {

  public DivideOperator(ExpressionNode left, ExpressionNode right,
                        Location location) {
    super(left, right, "/", location);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    if (v1.isNull() || v2.isNull())
      return NullNode.instance;

    // we only support the numeric operation and nothing else
    v1 = NodeUtils.number(v1, true, location);
    v2 = NodeUtils.number(v2, true, location);

    if (v1.isIntegralNumber() && v2.isIntegralNumber()) {
      long l1 = v1.longValue();
      long l2 = v2.longValue();
      if (l1 % l2 == 0)
        return new LongNode(l1 / l2);
      else
        return new DoubleNode((double) l1 / (double) l2);
    } else
      return new DoubleNode(perform(v1.doubleValue(), v2.doubleValue()));
  }

  protected double perform(double v1, double v2) {
    return v1 / v2;
  }

  // can't use this, because the integers are not closed under division
  protected long perform(long v1, long v2) {
    return v1 / v2; // uhh ... ?
  }
}
