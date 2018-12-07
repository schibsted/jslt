
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
import java.util.Deque;
import java.util.HashMap;
import java.util.ArrayDeque;
import com.schibsted.spt.data.jslt.JsltException;

/**
 * Keeps track of declared variables and maps them to their slots in
 * the stack frames.
 */
public class ScopeManager {
  private StackFrame globalFrame;
  private Deque<ScopeFrame> scopes;
  private StackFrame functionFrame;
  private Deque<ScopeFrame> functionScopes; // null when not in function

  // when inside function, this is functionScopes else scopes
  private Deque<ScopeFrame> current;
  private StackFrame currentFrame;

  public static final int UNFOUND = 0xFFFFFFFF;

  public ScopeManager() {
    this.globalFrame = new StackFrame();
    this.scopes = new ArrayDeque();
    this.current = scopes;
    this.currentFrame = globalFrame;
    enterScope();
  }

  public int getStackFrameSize() {
    return currentFrame.nextSlot;
  }

  /**
   * Called when we enter a new function. A function is not just a new
   * scope, because it needs its own stack frame.
   */
  public void enterFunction() {
    functionFrame = new StackFrame();
    functionScopes = new ArrayDeque();
    current = functionScopes;
    currentFrame = functionFrame;
    enterScope();
  }

  public void leaveFunction() {
    functionScopes = null;
    current = scopes;
    currentFrame = globalFrame;
  }

  /**
   * Called when we enter a new lexical scope in which variables can
   * be declared, hiding those declared further out. Although the
   * scopes are nested we flatten them into a single stack frame by
   * simply giving the variables different slots in the same frame.
   * Variable 'v' may map to different slots depending on where in the
   * code it is used.
   */
  public void enterScope() {
    current.push(new ScopeFrame(functionScopes != null, currentFrame));
  }

  public void leaveScope() {
    // we don't need this frame anymore (the variables remember their
    // own positions)
    current.pop();
  }

  public int registerVariable(String variable, Location loc) {
    // the slot combines two values in one: which stack frame the
    // variable resolves to, and its position in that frame. the first
    // bit says which frame, and the rest is left for the slot number.
    // basically:
    //  - if first bit set: function frame
    //  - if first bit not set: global frame
    return current.peek().registerVariable(variable, loc);
  }

  public int resolveVariable(VariableExpression variable) {
    String name = variable.getVariable();

    // traversing the scopes from top to bottom
    for (ScopeFrame scope : current) {
      int slot = scope.resolveVariable(name);
      if (slot != UNFOUND) // we found it
        return slot; // level is already part of the slot
    }

    // might have to traverse global scope, too
    if (functionScopes != null) {
      for (ScopeFrame scope : scopes) {
        int slot = scope.resolveVariable(name);
        if (slot != UNFOUND) // we found it
          return slot; // level is already part of the slot
      }
    }

    // FIXME FIXME FIXME
    // if we got here it means the variable was not found and we fail
    // HOWEVER, that's not the right solution, because what it means
    // is this variable must be defined from the outside. so what we
    // really should do is create a global slot for it and carry on

    // throw new JsltException("Reference to undefined variable " + name,
    //                         variable.getLocation());
    return UNFOUND; // FIXME it ain't right, but least we can move on
  }

  private static class ScopeFrame {
    private boolean inFunction;
    private StackFrame parent;
    private Map<String, Integer> variables;

    public ScopeFrame(boolean inFunction, StackFrame parent) {
      this.inFunction = inFunction;
      this.variables = new HashMap();
      this.parent = parent;
    }

    public int registerVariable(String variable, Location loc) {
      // see if we have a case of duplicate declaration
      if (variables.containsKey(variable))
        throw new JsltException("Duplicate variable declaration " +
                                variable, loc);

      // okay, register this variable
      int level = inFunction ? 0 : 0x10000000;
      int slot = level | parent.nextSlot++; // first free position
      variables.put(variable, slot);
      return slot;
    }

    public int resolveVariable(String name) {
      if (variables.containsKey(name))
        return variables.get(name);
      return UNFOUND;
    }
  }

  private static class StackFrame {
    private int nextSlot;
  }
}
