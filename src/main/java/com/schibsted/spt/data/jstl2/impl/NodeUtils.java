
package com.schibsted.spt.data.jstl2.impl;

import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NodeUtils {
  public static ObjectMapper mapper = new ObjectMapper();

  public static Scope evalLets(Scope scope, JsonNode input, LetExpression[] lets) {
    for (int ix = 0; ix < lets.length; ix++) {
      String var = lets[ix].getVariable();
      JsonNode val = lets[ix].apply(scope, input);
      scope = Scope.makeScope(Collections.singletonMap(var, val), scope);
    }
    return scope;
  }

  public static JsonNode toJson(boolean value) {
    if (value)
      return BooleanNode.TRUE;
    else
      return BooleanNode.FALSE;
  }

  public static JsonNode toJson(String[] array) {
    ArrayNode node = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < array.length; ix++)
      node.add(array[ix]);
    return node;
  }

  public static String indent(int level) {
    char[] indent = new char[level * 2];
    for (int ix = 0; ix < indent.length; ix++)
      indent[ix] = ' ';
    return new String(indent, 0, indent.length);
  }
}
