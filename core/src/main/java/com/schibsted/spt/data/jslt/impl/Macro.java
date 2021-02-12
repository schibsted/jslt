
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
import com.schibsted.spt.data.jslt.Callable;

/**
 * Interface implemented by all macros. A macro is like a function,
 * except that it controls the evaluation of its arguments itself.
 * That allows it to do things that an ordinary function cannot do.
 * Macros are an internal feature for now.
 */
public interface Macro extends Callable {

  /**
   * Invokes the macro, which can then modify the input node and
   * evaluate the parameters as needed.
   */
  public JsonNode call(Scope scope, JsonNode input, ExpressionNode[] parameters);

}
