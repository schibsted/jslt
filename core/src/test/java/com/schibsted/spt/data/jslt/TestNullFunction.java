
package com.schibsted.spt.data.jslt;

import com.schibsted.spt.data.json.JsonValue;

public class TestNullFunction implements Function {

  public String getName() {
    return "test";
  }

  public int getMinArguments() {
    return 0;
  }

  public int getMaxArguments() {
    return 0;
  }

  public JsonValue call(JsonValue input, JsonValue[] params) {
    // people are not supposed to do this, but they probably will
    return null;
  }
}
