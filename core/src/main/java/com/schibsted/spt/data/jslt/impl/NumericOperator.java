
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

public abstract class NumericOperator extends AbstractOperator {

  public NumericOperator(ExpressionNode left, ExpressionNode right, String name,
                         Location location) {
    super(left, right, name, location);
  }

  public JsonValue perform(JsonValue v1, JsonValue v2) {
    if (v1.isNull() || v2.isNull())
      return v1.makeNull();

    v1 = NodeUtils.number(v1, true, location);
    v2 = NodeUtils.number(v2, true, location);

    if (v1.isIntegralNumber() && v2.isIntegralNumber())
      return v1.makeValue(perform(v1.asLong(), v2.asLong()));
    else
      return v1.makeValue(perform(v1.asDouble(), v2.asDouble()));
  }

  protected abstract double perform(double v1, double v2);

  protected abstract long perform(long v1, long v2);
}
