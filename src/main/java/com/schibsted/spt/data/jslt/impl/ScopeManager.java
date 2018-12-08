
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
 * the stack frames. A stack frame is just an array, with one slot for
 * each variable. There are two kinds of stack frame: the global one,
 * which has top-level variables plus those from the top level of
 * modules. The second type is inside a function.
 *
 * <p>When a variable is declared so that it shadows an outer variable
 * those two get different slots, even though they have the same name.
 *
 * <p>The slot number combines two values in one: which stack frame
 * the variable resolves to, and its position in that frame. The first
 * bit says which frame, and the rest of the bits are left for the
 * slot number.
 *
 * <p>Basically:
 * <ul>
 *  <li>If first bit set: function frame
 *  <li>If first bit not set: global frame.
 * </ul>
 */
public class ScopeManager {
  private StackFrame globalFrame;
  private Deque<ScopeFrame> scopes;
  private StackFrame functionFrame;
  private Deque<ScopeFrame> functionScopes; // null when not in function

  // when inside function, this is functionScopes else scopes
  private Deque<ScopeFrame> current;
  private StackFrame currentFrame;

  // this is where we track the slots for parameters that must be
  // supplied from the outside
  private Map<String, Integer> parameterSlots;

  public static final int UNFOUND = 0xFFFFFFFF;

  public ScopeManager() {
    this.globalFrame = new StackFrame();
    this.scopes = new ArrayDeque();
    this.current = scopes;
    this.currentFrame = globalFrame;
    this.parameterSlots = new HashMap();
  }

  public int getStackFrameSize() {
    return currentFrame.nextSlot;
  }

  public Map<String, Integer> getParameterSlots() {
    return parameterSlots;
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

  /**
   * Registers a variable.
   */
  public VariableInfo registerVariable(LetExpression let) {
    LetInfo info = new LetInfo(let);
    current.peek().registerVariable(info);
    return info;
  }

  /**
   * Registers a parameter to a function.
   */
  public int registerParameter(String parameter, Location loc) {
    return current.peek().registerVariable(new ParameterInfo(parameter, loc));
  }

  public VariableInfo resolveVariable(VariableExpression variable) {
    String name = variable.getVariable();

    // traversing the scopes from top to bottom
    for (ScopeFrame scope : current) {
      VariableInfo var = scope.resolveVariable(name);
      if (var != null)
        return var;
    }

    // might have to traverse global scope, too
    if (functionScopes != null) {
      for (ScopeFrame scope : scopes) {
        VariableInfo var = scope.resolveVariable(name);
        if (var != null)
          return var;
      }
    }

    // if we got here it means the variable was not found. that means
    // it's not defined inside the JSLT expression, so it has to be
    // supplied as a parameter from outside during evaluation
    VariableInfo var = new ParameterInfo(name, variable.getLocation());
    int slot = scopes.getLast().registerVariable(var);
    parameterSlots.put(name, slot);
    return var;
  }

  /**
   * A scope frame is smaller than a stack frame: each object, object
   * comprehension, for expression, and if expression will have its
   * own scope frame. These need to be handled separately because of
   * the shadowing of variables.
   */
  private static class ScopeFrame {
    private boolean inFunction;
    private StackFrame parent;
    private Map<String, VariableInfo> variables;

    public ScopeFrame(boolean inFunction, StackFrame parent) {
      this.inFunction = inFunction;
      this.variables = new HashMap();
      this.parent = parent;
    }

    public int registerVariable(VariableInfo variable) {
      String name = variable.getName();

      // see if we have a case of duplicate declaration
      if (variables.containsKey(name))
        throw new JsltException("Duplicate variable declaration " +
                                name, variable.getLocation());

      // okay, register this variable
      int level = inFunction ? 0 : 0x10000000;
      int slot = level | parent.nextSlot++; // first free position
      variable.setSlot(slot);
      variables.put(name, variable);
      return slot;
    }

    public VariableInfo resolveVariable(String name) {
      return variables.get(name);
    }
  }

  private static class StackFrame {
    private int nextSlot;
  }
}
