
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

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jslt.JsltException;

public class ObjectComprehension extends AbstractNode {
  private ExpressionNode loop;
  private LetExpression[] lets;
  private ExpressionNode key;
  private ExpressionNode value;

  public ObjectComprehension(ExpressionNode loop,
                             ExpressionNode key,
                             ExpressionNode value,
                             Location location) {
    super(location);
    this.loop = loop;
    this.key = key;
    this.value = value;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode sequence = loop.apply(scope, input);
    if (sequence.isNull())
      return sequence;
    else if (sequence.isObject())
      sequence = NodeUtils.convertObjectToArray(sequence);
    else if (!sequence.isArray())
      throw new JsltException("Object comprehension can't loop over " + sequence, location);

    ObjectNode object = NodeUtils.mapper.createObjectNode();
    for (int ix = 0; ix < sequence.size(); ix++) {
      JsonNode context = sequence.get(ix);
      JsonNode keyNode = key.apply(scope, context);
      if (!keyNode.isTextual())
        throw new JsltException("Object comprehension must have string as key, not " + keyNode, location);
      JsonNode valueNode = value.apply(scope, context);
      if (NodeUtils.isValue(valueNode))
        object.set(keyNode.asText(), valueNode);
    }
    return object;
  }

  public void dump(int level) {
  }
}
