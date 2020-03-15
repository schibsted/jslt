
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

package com.schibsted.spt.data.jslt;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a compiled JSLT expression.
 */
public interface Expression {

  // this is an interface because we want to be able to produce
  // different kinds of compiled expressions, in addition to the
  // object structure interpreter that's there now.

  // it's different from the internal interface because we want to
  // have convenience methods without having to have those on every
  // kind of expression node internally (and vice versa)

  /**
   * Evaluate the expression on the given JSON input.
   * @param input The JSON input to evaluate the expression on.
   */
  public JsonNode apply(JsonNode input);

  /**
   * Evaluate the expression on the given JSON input, with the given
   * predefined variables set.
   * @param variables Variable bindings visible inside the expression.
   * @param input The JSON input to evaluate the expression on.
   */
  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input);

}
