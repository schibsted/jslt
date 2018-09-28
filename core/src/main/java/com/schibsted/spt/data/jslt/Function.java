
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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for function implementations.
 */
public interface Function extends Callable {

  // --- repeated from Callable

  /**
   * The name of the function.
   */
  public String getName();

  /**
   * The minimum number of arguments allowed.
   */
  public int getMinArguments();

  /**
   * The maximum number of arguments allowed.
   */
  public int getMaxArguments();

  // --- own methods

  /**
   * Perform the function on the given JSON input with the given arguments.
   */
  public JsonNode call(JsonNode input, JsonNode[] arguments);

}
