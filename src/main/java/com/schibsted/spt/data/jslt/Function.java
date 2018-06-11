
package com.schibsted.spt.data.jslt;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.Callable;

/**
 * Interface for function implementations.
 */
public interface Function extends Callable {

  // --- repeated from Callable
  public String getName();

  public int getMinArguments();

  public int getMaxArguments();

  // --- own methods
  public JsonNode call(JsonNode input, JsonNode[] arguments);

}
