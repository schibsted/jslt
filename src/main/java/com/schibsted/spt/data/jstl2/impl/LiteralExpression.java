
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class LiteralExpression extends AbstractNode {
  private JsonNode value;

  public LiteralExpression(JsonNode value) {
    this.value = value;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    return value;
  }

  public void compile(Compiler compiler) {
    compiler.genPUSHL(value);
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + value);
  }
}
