
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
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.Expression;

/**
 * Wrapper class that translates an external Expression to an
 * ExpressionNode.
 */
public class ExpressionImpl implements Expression {
  private LetExpression[] lets;
  private Map<String, Function> functions;
  private ExpressionNode actual;
  private int stackFrameSize;
  private JstlFile[] fileModules;

  // contains the mapping from external parameters (variables set from
  // outside at query-time) to slots, so that we can put the
  // parameters into the scope when evaluating the query
  private Map<String, Integer> parameterSlots;

  public ExpressionImpl(LetExpression[] lets, Map<String, Function> functions,
                        ExpressionNode actual) {
    this.lets = lets;
    this.functions = functions;
    this.actual = actual;

    // traverse tree and set up context queries
    DotExpression root = new DotExpression(null);
    if (actual != null)
      actual.computeMatchContexts(root);
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].computeMatchContexts(root);
  }

  public Function getFunction(String name) {
    return functions.get(name);
  }

  public boolean hasBody() {
    return actual != null;
  }

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input) {
    Scope scope = Scope.makeScope(variables, stackFrameSize, parameterSlots);
    return apply(scope, input);
  }

  public JsonNode apply(JsonNode input) {
    return apply(Scope.getRoot(stackFrameSize), input);
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    // Jackson 2.9.2 can parse to Java null. See unit test
    // QueryTest.testNullInput. so we have to handle that
    if (input == null)
      input = NullNode.instance;

    // evaluate lets in global modules
    if (fileModules != null) {
      for (int ix = 0; ix < fileModules.length; ix++)
        fileModules[ix].evaluateLetsOnly(scope, input);
    }

    // evaluate own lets
    NodeUtils.evalLets(scope, input, lets);

    return actual.apply(scope, input);
  }

  public void dump() {
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].dump(0);
    actual.dump(0);
  }

  public void prepare(PreparationContext ctx) {
    ctx.scope.enterScope();
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].register(ctx.scope);

    for (ExpressionNode child : getChildren())
      child.prepare(ctx);

    stackFrameSize = ctx.scope.getStackFrameSize();
    parameterSlots = ctx.scope.getParameterSlots();
    ctx.scope.leaveScope();
  }

  /**
   * This is used to initialize global variables when the
   * ExpressionImpl is a module. Called once during compilation.
   * The values are then remembered forever.
   */
  public void evaluateLetsOnly(Scope scope, JsonNode input) {
    NodeUtils.evalLets(scope, input, lets);
  }

  public void optimize() {
    lets = OptimizeUtils.optimizeLets(lets);

    for (Function f : functions.values())
      if ((f instanceof FunctionDeclaration))
        ((FunctionDeclaration) f).optimize();

    if (actual != null)
      actual = actual.optimize();
  }

  public List<ExpressionNode> getChildren() {
    List<ExpressionNode> children = new ArrayList();
    children.addAll(Arrays.asList(lets));
    for (Function f : functions.values())
      if ((f instanceof FunctionDeclaration))
        children.add((FunctionDeclaration) f);
    if (actual != null)
      children.add(actual);
    return children;
  }

  public String toString() {
    // FIXME: letexprs
    return actual.toString();
  }

  public int getStackFrameSize() {
    return stackFrameSize;
  }

  public void setGlobalModules(List<JstlFile> fileModules) {
    this.fileModules = new JstlFile[fileModules.size()];
    this.fileModules = fileModules.toArray(this.fileModules);
  }
}
