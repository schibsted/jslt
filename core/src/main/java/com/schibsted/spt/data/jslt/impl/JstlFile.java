
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
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.Module;
import com.schibsted.spt.data.jslt.Callable;
import com.schibsted.spt.data.jslt.Function;

/**
 * Represents a JSLT source code file loaded separately.
 */
public class JstlFile implements Module, Function {
  private String prefix;
  private ExpressionImpl body;
  private String source; // where was the file loaded from?

  public JstlFile(String prefix, String source, ExpressionImpl body) {
    this.prefix = prefix;
    this.source = source;
    this.body = body;
  }

  // the module part

  public Callable getCallable(String name) {
    return body.getFunction(name);
  }

  // the function part

  public String getName() {
    return prefix;
  }

  public int getMinArguments() {
    return 1;
  }

  public int getMaxArguments() {
    return 1;
  }

  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    if (!body.hasBody())
      throw new JsltException("Module '" + prefix + "' has no body, so cannot "+
                              "be called as a function");

    // make the argument be the input to the template
    return body.apply(arguments[0]);
  }

  public void evaluateLetsOnly(Scope scope, JsonNode input) {
    body.evaluateLetsOnly(scope, input);
  }
}
