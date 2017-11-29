
package com.schibsted.spt.data.jstl2;

import com.fasterxml.jackson.databind.JsonNode;

public interface Expression {

  public JsonNode apply(JsonNode input);

}
