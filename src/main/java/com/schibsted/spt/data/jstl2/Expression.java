
package com.schibsted.spt.data.jstl2;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * External representation of compiled JSTL 2.0 expressions.
 */
public interface Expression {

  // this is an interface because we want to be able to produce
  // different kinds of compiled expressions, in addition to the
  // trivial object structure interpreter that's there now.

  // it's different from the internal interface because we want to
  // have convenience methods without having to have those on every
  // kind of expression node internally

  public JsonNode apply(JsonNode input);

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input);

}
