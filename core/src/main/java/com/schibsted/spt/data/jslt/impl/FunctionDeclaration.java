
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
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;

public class FunctionDeclaration extends AbstractNode
  implements Function, ExpressionNode {

  private String name;
  private String[] parameters;
  private int[] parameterSlots;
  private LetExpression[] lets;
  private ExpressionNode body;
  private int stackFrameSize;

  public FunctionDeclaration(String name, String[] parameters,
                             LetExpression[] lets, ExpressionNode body) {
    super(null);
    this.name = name;
    this.parameters = parameters;
    this.parameterSlots = new int[parameters.length];
    this.lets = lets;
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public int getMinArguments() {
    return parameters.length;
  }

  public int getMaxArguments() {
    return parameters.length;
  }

  // this method is here because the Function signature requires it,
  // but we can't actually use it, because a declared function needs
  // (or at least may need) access to the global scope. in order to be
  // able to treat FunctionDeclaration like other Functions we resort
  // to this solution for now.
  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    throw new JsltException("INTERNAL ERROR!");
  }

  public JsonNode call(Scope scope, JsonNode input, JsonNode[] arguments) {
    scope.enterFunction(stackFrameSize);

    // bind the arguments into the function scope
    for (int ix = 0; ix < arguments.length; ix++)
      scope.setValue(parameterSlots[ix], arguments[ix]);

    // then bind the lets
    NodeUtils.evalLets(scope, input, lets);

    // evaluate body
    JsonNode value = body.apply(scope, input);
    scope.leaveFunction();
    return value;
  }

  public ExpressionNode optimize() {
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].optimize();

    body = body.optimize();
    return this;
  }

  // the ExpressionNode API requires this method, but it doesn't
  // actually make any sense for a Function
  public JsonNode apply(Scope scope, JsonNode context) {
    throw new JsltException("INTERNAL ERROR");
  }

  public void computeMatchContexts(DotExpression parent) {
    // not allowed to use object matcher inside declared functions
    FailDotExpression fail = new FailDotExpression(null, "function declaration");
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].computeMatchContexts(fail);
    body.computeMatchContexts(fail);
  }

  public void prepare(PreparationContext ctx) {
    ctx.scope.enterFunction();

    for (int ix = 0; ix < parameters.length; ix++)
      parameterSlots[ix] = ctx.scope.registerParameter(parameters[ix], location);

    for (int ix = 0; ix < lets.length; ix++) {
      lets[ix].register(ctx.scope);
      lets[ix].prepare(ctx);
    }

    body.prepare(ctx);

    stackFrameSize = ctx.scope.getStackFrameSize();
    ctx.scope.leaveFunction();
  }
}
