
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
import com.schibsted.spt.data.jslt.JsltException;

public class MultiplyOperator extends NumericOperator {

  public MultiplyOperator(ExpressionNode left, ExpressionNode right,
                          Location location) {
    super(left, right, "*", location);
  }

  public JsonNode perform(JsonNode v1, JsonNode v2) {
    if (v1.isTextual() || v2.isTextual()) {
      // if one operand is string: do string multiplication

      String str;
      int num;
      if (v1.isTextual() && !v2.isTextual()) {
        str = v1.asText();
        num = v2.intValue();
      } else if (v2.isTextual()) {
        str = v2.asText();
        num = v1.intValue();
      } else
        throw new JsltException("Can't multiply two strings!");

      StringBuilder buf = new StringBuilder();
      for ( ; num > 0; num--)
        buf.append(str);

      return new TextNode(buf.toString());
    } else
      // do numeric operation
      return super.perform(v1, v2);
  }

  protected double perform(double v1, double v2) {
    return v1 * v2;
  }

  protected long perform(long v1, long v2) {
    return v1 * v2;
  }
}
