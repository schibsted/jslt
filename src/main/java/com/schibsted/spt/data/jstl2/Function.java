
package com.schibsted.spt.data.jstl2;

import com.fasterxml.jackson.databind.JsonNode;

public interface Function {

  public String getName();

  public int getMinArguments();

  public int getMaxArguments();

  public JsonNode call(JsonNode input, Expression[] arguments);

}
