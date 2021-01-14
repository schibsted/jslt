
// Copyright 2018 Schibsted Marketplaces Products & Technology As
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.schibsted.spt.data.jslt.impl;

import java.math.BigInteger;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.schibsted.spt.data.jslt.JsltException;

public class NodeUtils {
  public static final ObjectMapper mapper = new ObjectMapper();

  public static void evalLets(Scope scope, JsonNode input, LetExpression[] lets) {
    if (lets == null)
      return;

    for (int ix = 0; ix < lets.length; ix++) {
      String var = lets[ix].getVariable();
      JsonNode val = lets[ix].apply(scope, input);
      scope.setValue(lets[ix].getSlot(), val);
    }
  }

  public static boolean isTrue(JsonNode value) {
    return value != BooleanNode.FALSE &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isTextual() && value.asText().length() == 0) &&
      !(value.isArray() && value.size() == 0) &&
      !(value.isNumber() && value.doubleValue() == 0.0) &&
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

  public static JsonNode toJson(double value) {
    return new DoubleNode(value);
  }

  public static JsonNode toJson(String[] array) {
    ArrayNode node = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < array.length; ix++)
      node.add(array[ix]);
    return node;
  }

  // nullok => return Java null for Json null
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

    throw new JsltException("Cannot convert " + value + " to array");
  }

  public static JsonNode number(JsonNode value, Location loc) {
    return number(value, false, loc);
  }

  public static JsonNode number(JsonNode value, boolean strict, Location loc) {
    // this works, because Java null can never be a function parameter
    // in JSLT, unlike JSON null
    return number(value, strict, loc, null);
  }

  public static JsonNode number(JsonNode value, boolean strict, Location loc,
                                JsonNode fallback) {
    // check what type this is
    if (value.isNumber())
      return value;
    else if (value.isNull()) {
      if (fallback == null)
        return value;
      else
        return fallback;
    } else if (!value.isTextual()) {
      if (strict)
        throw new JsltException("Can't convert " + value + " to number", loc);
      else if (fallback == null)
        return NullNode.instance;
      else
        return fallback;
    }

    // let's look at this number
    String number = value.asText();
    JsonNode numberNode = parseNumber(number);
    if (numberNode == null || !numberNode.isNumber()) {
      if (fallback == null)
        throw new JsltException("number(" + number + ") failed: not a number",
                                loc);
      else
        return fallback;
    } else {
        return numberNode;
    }
  }

  // returns null in case of failure (caller then handles fallback)
  private static JsonNode parseNumber(String number) {
    if (number.length() == 0)
      return null;

    int pos = 0;
    if (number.charAt(0) == '-') {
      pos = 1;
    }

    int endInteger = scanDigits(number, pos);
    if (endInteger == pos)
      return null;
    if (endInteger == number.length()) {
      if (number.length() < 10)
        return new IntNode(Integer.parseInt(number));
      else if (number.length() < 19)
        return new LongNode(Long.parseLong(number));
      else
        return new BigIntegerNode(new BigInteger(number));
    }

    // since there's stuff after the initial integer it must be either
    // the decimal part or the exponent
    long intPart = Long.parseLong(number.substring(0, endInteger));
    pos = endInteger;
    double value = intPart;

    if (number.charAt(pos) == '.') {
      pos += 1;
      int endDecimal = scanDigits(number, pos);
      if (endDecimal == pos)
        return null;

      long decimalPart = Long.parseLong(number.substring(endInteger + 1, endDecimal));
      int digits = endDecimal - endInteger - 1;

      // if intPart is negative we can't add a positive decimalPart to it
      if (intPart < 0)
        decimalPart = decimalPart * -1;

      value = (decimalPart / Math.pow(10, digits)) + intPart;
      pos = endDecimal;

      // if there's nothing more, then this is it
      if (pos == number.length())
        return new DoubleNode(value);
    }

    // there is more: next character MUST be 'e' or 'E'
    char ch = number.charAt(pos);
    if (ch != 'e' && ch != 'E')
      return null;

    // now we must have either '-', '+', or an integer
    pos++;
    if (pos == number.length())
      return null;
    ch = number.charAt(pos);
    int sign = 1;
    if (ch == '+')
      pos++;
    else if (ch == '-') {
      sign = -1;
      pos++;
    }

    int endExponent = scanDigits(number, pos);
    if (endExponent != number.length() || endExponent == pos)
      return null;

    int exponent = Integer.parseInt(number.substring(pos)) * sign;
    return new DoubleNode(value * Math.pow(10, exponent));
  }

  private static int scanDigits(String number, int pos) {
    while (pos < number.length() && isDigit(number.charAt(pos)))
      pos++;
    return pos;
  }

  private static boolean isDigit(char ch) {
    return ch >= '0' && ch <= '9';
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
