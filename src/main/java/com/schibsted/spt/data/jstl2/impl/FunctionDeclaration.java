
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.Function;

public class FunctionDeclaration implements Function {
  private String name;
  private String[] parameters;
  private LetExpression[] lets;
  private ExpressionNode body;

  public FunctionDeclaration(String name, String[] parameters,
                             LetExpression[] lets, ExpressionNode body) {
    this.name = name;
    this.parameters = parameters;
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

  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    // build scope inside function body, params first
    Map<String, JsonNode> params = new HashMap(arguments.length);
    for (int ix = 0; ix < arguments.length; ix++)
      params.put(parameters[ix], arguments[ix]);
    Scope scope = Scope.makeScope(params);

    // then lets
    if (lets.length > 0)
      scope = NodeUtils.evalLets(scope, input, lets);

    // evaluate body
    return body.apply(scope, input);
  }
}
