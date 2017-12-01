
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Internal interface for the parts of a compiled JSTL 2.0 expression.
 * Different from the external interface because we want to avoid
 * having convenience methods here, and also because we may want to
 * add methods for introspection (for optimization, generating
 * byte-code, etc).
 */
public interface ExpressionNode {

  public JsonNode apply(Scope scope, JsonNode input);

}
