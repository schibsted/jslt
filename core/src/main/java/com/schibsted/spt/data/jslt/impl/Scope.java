
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
import java.util.Collections;
import java.util.Deque;
import java.util.ArrayDeque;
import com.fasterxml.jackson.databind.JsonNode;

public class Scope {
  public static Scope getRoot(int stackFrameSize) {
    return new Scope(stackFrameSize);
  }

  /**
   * Creates an initialized scope with values for variables supplied
   * by client code into the JSLT expression.
   */
  public static Scope makeScope(Map<String, JsonNode> variables,
                                int stackFrameSize,
                                Map<String, Integer> parameterSlots) {
    Scope scope = new Scope(stackFrameSize);
    for (String variable : variables.keySet())
      if (parameterSlots.containsKey(variable)) // check that variable exists
        scope.setValue(parameterSlots.get(variable), variables.get(variable));
    return scope;
  }

  private JsonNode[] globalStackFrame;
  private Deque<JsonNode[]> localStackFrames;
  private static final int BITMASK = 0x10000000;
  private static final int INVERSE = 0xEFFFFFFF;

  public Scope(int stackFrameSize) {
    this.globalStackFrame = new JsonNode[stackFrameSize];
    this.localStackFrames = new ArrayDeque();
  }

  public void enterFunction(int stackFrameSize) {
    localStackFrames.push(new JsonNode[stackFrameSize]);
  }

  public void leaveFunction() {
    localStackFrames.pop();
  }

  public JsonNode getValue(int slot) {
    if ((slot & BITMASK) != 0)
      return globalStackFrame[slot & INVERSE];
    else
      return localStackFrames.peek()[slot];
  }

  public void setValue(int slot, JsonNode value) {
    if ((slot & BITMASK) != 0)
      globalStackFrame[slot & INVERSE] = value;
    else
      localStackFrames.peek()[slot] = value;
  }
}
