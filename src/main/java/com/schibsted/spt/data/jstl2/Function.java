
package com.schibsted.spt.data.jstl2;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.impl.Callable;

/**
 * Interface implemented by all function implementations. May replace
 * it with a wrapper that carries the metadata and an inner object
 * that has just the 'call' method.
 */
public interface Function extends Callable {

  // --- repeated from Callable
  public String getName();

  public int getMinArguments();

  public int getMaxArguments();

  // --- own methods
  public JsonNode call(JsonNode input, JsonNode[] arguments);

}
