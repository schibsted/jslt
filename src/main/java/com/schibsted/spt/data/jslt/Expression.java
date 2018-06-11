
package com.schibsted.spt.data.jslt;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a compiled JSLT expression.
 */
public interface Expression {

  // this is an interface because we want to be able to produce
  // different kinds of compiled expressions, in addition to the
  // object structure interpreter that's there now.

  // it's different from the internal interface because we want to
  // have convenience methods without having to have those on every
  // kind of expression node internally

  /**
   * Evaluate the expression on the given JSON input.
   * @param input The JSON input to evaluate the expression on.
   */
  public JsonNode apply(JsonNode input);

  /**
   * Evaluate the expression on the given JSON input, with the given
   * predefined variables set.
   * @param variables Variable bindings visible inside the expression.
   * @param input The JSON input to evaluate the expression on.
   */
  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input);

}
