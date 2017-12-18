
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jstl2.JstlException;

public class NodeUtils {
  public static final ObjectMapper mapper = new ObjectMapper();

  public static Scope evalLets(Scope scope, JsonNode input, LetExpression[] lets) {
    for (int ix = 0; ix < lets.length; ix++) {
      String var = lets[ix].getVariable();
      JsonNode val = lets[ix].apply(scope, input);
      scope = Scope.makeScope(Collections.singletonMap(var, val), scope);
    }
    return scope;
  }

  public static boolean isTrue(JsonNode value) {
    return value != BooleanNode.FALSE &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isTextual() && value.asText().length() == 0) &&
      !(value.isArray() && value.size() == 0) &&
      !value.isNull();
  }

  public static boolean isValue(JsonNode value) {
    return !value.isNull() &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isArray() && value.size() == 0);
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

  public static ArrayNode toArray(JsonNode value, boolean nullok) {
    // check what type this is
    if (value.isArray())
      return (ArrayNode) value;
    else if (value.isNull() && nullok)
      return null;

    throw new JstlException("Cannot convert " + value + " to array");
  }

  public static JsonNode number(JsonNode value, Location loc) {
    return number(value, false, loc);
  }

  public static JsonNode number(JsonNode value, boolean strict, Location loc) {
    // check what type this is
    if (value.isNumber())
      return value;
    else if (!value.isTextual()) {
      if (strict)
        throw new JstlException("Can't convert " + value + " to number", loc);
      else
        return NullNode.instance;
    }

    // let's look at this number
    String number = value.asText();
    try {
      if (number.indexOf('.') != -1)
        return new DoubleNode(Double.parseDouble(number));
      else
        return new IntNode(Integer.parseInt(number));
    } catch (NumberFormatException e) {
      throw new JstlException("number(" + number + ") failed: not a number",
                              loc);
    }
  }

  public static ArrayNode convertObjectToArray(JsonNode object) {
    ArrayNode array = mapper.createArrayNode();
    Iterator<Map.Entry<String, JsonNode>> it = object.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> item = it.next();
      ObjectNode element = NodeUtils.mapper.createObjectNode();
      element.set("key", new TextNode(item.getKey()));
      element.set("value", item.getValue());
      array.add(element);
    }
    return array;
  }

  public static String indent(int level) {
    char[] indent = new char[level * 2];
    for (int ix = 0; ix < indent.length; ix++)
      indent[ix] = ' ';
    return new String(indent, 0, indent.length);
  }
}
