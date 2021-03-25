
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

import com.schibsted.spt.data.json.JsonValue;
import com.schibsted.spt.data.jslt.JsltException;

public abstract class ComparisonOperator extends AbstractOperator {

  public ComparisonOperator(ExpressionNode left, ExpressionNode right,
                            String operator, Location location) {
    super(left, right, operator, location);
  }

  public abstract JsonValue perform(JsonValue v1, JsonValue v2);

  public double compare(JsonValue v1, JsonValue v2) {
    return compare(v1, v2, location);
  }

  public static double compare(JsonValue v1, JsonValue v2, Location location) {
    if (v1.isNumber() && v2.isNumber()) {
      double n1 = NodeUtils.number(v1, location).asDouble();
      double n2 = NodeUtils.number(v2, location).asDouble();
      return n1 - n2;

    } else if (v1.isString() && v2.isString()) {
      String s1 = v1.asString();
      String s2 = v2.asString();
      return (double) s1.compareTo(s2);

    } else if (v1.isNull() || v2.isNull()) {
      // null is equal to itself, and considered the smallest of all
      if (v1.isNull() && v2.isNull())
        return 0;
      else if (v1.isNull())
        return -1;
      else
        return 1;
    }

    throw new JsltException("Can't compare " + v1 + " and " + v2, location);
  }

}
