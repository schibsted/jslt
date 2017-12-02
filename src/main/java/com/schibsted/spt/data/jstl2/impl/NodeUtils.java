
package com.schibsted.spt.data.jstl2.impl;

import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jstl2.JstlException;

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

  public static String toString(JsonNode value, boolean nullok) {
    // check what type this is
    if (value.isTextual())
      return value.asText();
    else if (value.isNull() && nullok)
      return null;

    // not sure how well this works in practice, but let's try
    return value.toString();
  }

  public static JsonNode number(JsonNode value) {
    // check what type this is
    if (value.isNumber())
      return value;
    else if (!value.isTextual())
      return NullNode.instance;

    // let's look at this number
    String number = value.asText();
    try {
      if (number.indexOf('.') != -1)
        return new DoubleNode(Double.parseDouble(number));
      else
        return new IntNode(Integer.parseInt(number));
    } catch (NumberFormatException e) {
      throw new JstlException("number(" + number + ") failed: not a number");
    }
  }

  public static String indent(int level) {
    char[] indent = new char[level * 2];
    for (int ix = 0; ix < indent.length; ix++)
      indent[ix] = ' ';
    return new String(indent, 0, indent.length);
  }
}
