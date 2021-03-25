
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

import com.schibsted.spt.data.json.*;
import com.schibsted.spt.data.jslt.JsltException;

public class DivideOperator extends NumericOperator {

  public DivideOperator(ExpressionNode left, ExpressionNode right,
                        Location location) {
    super(left, right, "/", location);
  }

  public JsonValue perform(JsonValue v1, JsonValue v2) {
    if (v1.isNull() || v2.isNull())
      return v1.makeNull();

    // we only support the numeric operation and nothing else
    v1 = NodeUtils.number(v1, true, location);
    v2 = NodeUtils.number(v2, true, location);

    if (v1.isIntegralNumber() && v2.isIntegralNumber()) {
      long l1 = v1.asLong();
      long l2 = v2.asLong();
      if (l1 % l2 == 0)
        return v1.makeValue(l1 / l2);
      else
        return v1.makeValue((double) l1 / (double) l2);
    } else
      return v1.makeValue(perform(v1.asDouble(), v2.asDouble()));
  }

  protected double perform(double v1, double v2) {
    return v1 / v2;
  }

  // can't use this, because the integers are not closed under division
  protected long perform(long v1, long v2) {
    return v1 / v2; // uhh ... ?
  }
}
