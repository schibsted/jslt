
package com.schibsted.spt.data.jstl2;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface implemented by all function implementations. May replace
 * it with a wrapper that carries the metadata and an inner object
 * that has just the 'call' method. May introduce a MagicFunction that
 */
public interface Function {

  public String getName();

  public int getMinArguments();

  public int getMaxArguments();

  public JsonNode call(JsonNode input, JsonNode[] arguments);

}
