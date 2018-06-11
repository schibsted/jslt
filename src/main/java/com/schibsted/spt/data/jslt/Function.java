
package com.schibsted.spt.data.jslt;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.Callable;

/**
 * Interface for function implementations.
 */
public interface Function extends Callable {

  // --- repeated from Callable

  /**
   * The name of the function.
   */
  public String getName();

  /**
   * The minimum number of arguments allowed.
   */
  public int getMinArguments();

  /**
   * The maximum number of arguments allowed.
   */
  public int getMaxArguments();

  // --- own methods

  /**
   * Perform the function on the given JSON input with the given arguments.
   */
  public JsonNode call(JsonNode input, JsonNode[] arguments);

}
