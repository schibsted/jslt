
package com.schibsted.spt.data.jstl2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;

import com.schibsted.spt.data.jslt.Function;

public class TestFunction implements Function {

  public String getName() {
    return "test";
  }

  public int getMinArguments() {
    return 0;
  }

  public int getMaxArguments() {
    return 0;
  }

  public JsonNode call(JsonNode input, JsonNode[] params) {
    return new IntNode(42);
  }
}
