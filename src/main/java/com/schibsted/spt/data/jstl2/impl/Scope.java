
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.JstlException;

public class Scope {
  private static Scope root = new Scope(Collections.EMPTY_MAP, null);

  public static Scope getRoot() {
    return root;
  }

  public static Scope makeScope(Map<String, JsonNode> variables) {
    return new Scope(variables, null);
  }

  public static Scope makeScope(Map<String, JsonNode> variables, Scope parent) {
    return new Scope(variables, parent);
  }

  private Scope parent;
  private Map<String, JsonNode> variables;

  private Scope(Map<String, JsonNode> variables, Scope parent) {
    this.parent = parent;
    this.variables = variables;
  }

  public JsonNode getValue(String variable) {
    JsonNode value = variables.get(variable);
    if (value == null && parent != null)
      value = parent.getValue(variable);
    if (value == null)
      throw new JstlException("No such variable '" + variable + "'");
    return value;
  }
}
