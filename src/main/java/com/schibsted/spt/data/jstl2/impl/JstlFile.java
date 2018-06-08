
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

/**
 * Represents a JSTL source code file loaded separately.
 */
public class JstlFile implements Module, Function {
  private String prefix;
  private ExpressionImpl body;
  private String source; // where was the file loaded from?

  public JstlFile(String prefix, String source, ExpressionImpl body) {
    this.prefix = prefix;
    this.source = source;
    this.body = body;
  }

  // the module part

  public Function getFunction(String name) {
    return body.getFunction(name);
  }

  // the function part

  public String getName() {
    return prefix;
  }

  public int getMinArguments() {
    return 1;
  }

  public int getMaxArguments() {
    return 1;
  }

  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    if (!body.hasBody())
      throw new JstlException("Module '" + prefix + "' has no body, so cannot "+
                              "be called as a function");

    // make the argument be the input to the template
    return body.apply(arguments[0]);
  }
}
