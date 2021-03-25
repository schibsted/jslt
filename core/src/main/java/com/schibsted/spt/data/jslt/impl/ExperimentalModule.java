
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

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import com.schibsted.spt.data.json.*;
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

    public JsonValue call(Scope scope, JsonValue input,
                          ExpressionNode[] parameters) {
      // this has to be a macro, because the second argument needs to be
      // evaluated in a special context

      // first find the array that we iterate over
      JsonValue array = parameters[0].apply(scope, input);
      if (array.isNull())
        return input.makeNull();
      else if (array.isObject())
        array = NodeUtils.convertObjectToArray(array);
      else if (!array.isArray())
        throw new JsltException("Can't group-by on " + array);

      // now start grouping
      Map<JsonValue, List<JsonValue>> groups = new HashMap();
      for (int ix = 0; ix < array.size(); ix++) {
        JsonValue groupInput = array.get(ix);
        JsonValue key = parameters[1].apply(scope, groupInput);
        JsonValue value = parameters[2].apply(scope, groupInput);

        // FIXME: not quite convinced this is the way to do it
        List<JsonValue> values = groups.get(key);
        if (values == null) {
          values = new ArrayList<>();
          groups.put(key, values);
        }
        values.add(value);
      }

      // grouping is done, build JSON output
      int ix = 0;
      JsonValue[] buffer = new JsonValue[groups.size()];
      for (JsonValue key : groups.keySet()) {
        buffer[ix++] = input.makeObjectBuilder()
          .set("key", key)
          .set("values", input.makeArray(groups.get(key)))
          .build();
      }
      return input.makeArray(buffer);
    }
  }

}
