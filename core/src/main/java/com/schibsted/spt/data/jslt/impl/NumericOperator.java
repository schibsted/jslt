
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

public abstract class NumericOperator extends AbstractOperator {

  public NumericOperator(ExpressionNode left, ExpressionNode right, String name,
                         Location location) {
    super(left, right, name, location);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    if (v1.isNull() || v2.isNull())
      return NullNode.instance;

    v1 = NodeUtils.number(v1, true, location);
    v2 = NodeUtils.number(v2, true, location);

    if (v1.isIntegralNumber() && v2.isIntegralNumber())
      return new LongNode(perform(v1.longValue(), v2.longValue()));
    else
      return new DoubleNode(perform(v1.doubleValue(), v2.doubleValue()));
  }

  protected abstract double perform(double v1, double v2);

  protected abstract long perform(long v1, long v2);
}
