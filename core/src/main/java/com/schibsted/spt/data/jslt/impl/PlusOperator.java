
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
import com.schibsted.spt.data.json.JsonObjectBuilder;
import com.schibsted.spt.data.jslt.JsltException;

public class PlusOperator extends NumericOperator {

  public PlusOperator(ExpressionNode left, ExpressionNode right,
                      Location location) {
    super(left, right, "+", location);
  }

  public JsonValue perform(JsonValue v1, JsonValue v2) {
    if (v1.isString() || v2.isString()) {
      // if one operand is string: do string concatenation
      return v1.makeValue(NodeUtils.toString(v1, false) +
                          NodeUtils.toString(v2, false));

    } else if (v1.isArray() && v2.isArray())
      // if both are arrays: array concatenation
      return concatenateArrays(v1, v2);

    else if (v1.isObject() && v2.isObject())
      // if both are objects: object union
      return unionObjects(v1, v2);

    // {} + null => {} (also arrays)
    else if ((v1.isObject() || v1.isArray()) && v2.isNull())
      return v1;

    // null + {} => {} (also arrays)
    else if (v1.isNull() && (v2.isObject() || v2.isArray()))
      return v2;

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

  private JsonValue concatenateArrays(JsonValue v1, JsonValue v2) {
    JsonValue[] buffer = new JsonValue[v1.size() + v2.size()];
    int pos = 0;
    for (int ix = 0; ix < v1.size(); ix++)
      buffer[pos++] = v1.get(ix);
    for (int ix = 0; ix < v2.size(); ix++)
      buffer[pos++] = v2.get(ix);
    return v1.makeArray(buffer);
  }

  private JsonValue unionObjects(JsonValue v1, JsonValue v2) {
    JsonObjectBuilder object = v1.makeObjectBuilder();
    // FIXME: figure out how to do this efficiently
    // result.putAll((ObjectNode) v2);
    // result.putAll((ObjectNode) v1); // v1 should overwrite v2
    return object.build();
  }
}
