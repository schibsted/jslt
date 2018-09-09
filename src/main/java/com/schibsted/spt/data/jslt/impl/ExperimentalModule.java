
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

import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jslt.Module;
import com.schibsted.spt.data.jslt.Callable;
import com.schibsted.spt.data.jslt.JsltException;

/**
 * A module containing functions and macros that *may* be officially
 * added to JSLT in the future. For now, they're made available here
 * so that people can use them and we can build experience with these
 * implementations.
 */
public class ExperimentalModule implements Module {
  public static final String URI = "http://jslt.schibsted.com/2018/experimental";
  private Map<String, Callable> callables = new HashMap();

  public ExperimentalModule() {
    register(new GroupBy());
  }

  public Callable getCallable(String name) {
    return callables.get(name);
  }

  private void register(Callable callable) {
    callables.put(callable.getName(), callable);
  }

  public static class GroupBy extends AbstractCallable implements Macro {

    public GroupBy() {
      super("group-by", 3, 3);
    }

    public JsonNode call(Scope scope, JsonNode input,
                         ExpressionNode[] parameters) {
      // this has to be a macro, because the second argument needs to be
      // evaluated in a special context

      // first find the array that we iterate over
      JsonNode array = parameters[0].apply(scope, input);
      if (array.isNull())
        return NullNode.instance;
      else if (array.isObject())
        array = NodeUtils.convertObjectToArray(array);
      else if (!array.isArray())
        throw new JsltException("Can't group-by on " + array);

      // now start grouping
      ObjectNode result = NodeUtils.mapper.createObjectNode();
      for (int ix = 0; ix < array.size(); ix++) {
        JsonNode groupInput = array.get(ix);
        JsonNode key = parameters[1].apply(scope, groupInput);
        JsonNode value = parameters[2].apply(scope, groupInput);

        String keyString = NodeUtils.toString(key, false);
        ArrayNode values = (ArrayNode) result.get(keyString);
        if (values == null) {
          values = NodeUtils.mapper.createArrayNode();
          result.set(keyString, values);
        }
        values.add(value);
      }

      return result;
    }
  }

}
