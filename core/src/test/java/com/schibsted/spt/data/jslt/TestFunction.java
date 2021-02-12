
package com.schibsted.spt.data.jslt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;

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
