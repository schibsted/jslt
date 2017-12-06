
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class VariableExpression extends AbstractNode {
  private String variable;

  public VariableExpression(String variable) {
    this.variable = variable;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return scope.getValue(variable);
  }

  public void compile(Compiler compiler) {
    compiler.genPUSHV(variable);
  }

  public void dump(int level) {
  }
}
