
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
import com.schibsted.spt.data.json.*;
import com.schibsted.spt.data.jslt.JsltException;

public class NodeUtils {

  public static void evalLets(Scope scope, JsonValue input, LetExpression[] lets) {
    if (lets == null)
      return;

    for (int ix = 0; ix < lets.length; ix++) {
      String var = lets[ix].getVariable();
      JsonValue val = lets[ix].apply(scope, input);
      scope.setValue(lets[ix].getSlot(), val);
    }
  }

  public static boolean isTrue(JsonValue value) {
    return !(value.isBoolean() && !value.asBoolean()) &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isString() && value.size() == 0) &&
      !(value.isArray() && value.size() == 0) &&
      !(value.isNumber() && value.asDouble() == 0.0) &&
      !value.isNull();
  }

  public static boolean isValue(JsonValue value) {
    return !value.isNull() &&
      !(value.isObject() && value.size() == 0) &&
      !(value.isArray() && value.size() == 0);
  }

  public static JsonValue toJson(boolean value) {
    if (value)
      return BooleanJValue.TRUE;
    else
      return BooleanJValue.FALSE;
  }

  // public static JsonValue toJson(double value) {
  //   return new DoubleNode(value);
  // }

  // public static JsonValue toJson(String[] array) {
  //   ArrayNode node = NodeUtils.mapper.createArrayNode();
  //   for (int ix = 0; ix < array.length; ix++)
  //     node.add(array[ix]);
  //   return node;
  // }

  // nullok => return Java null for Json null
  public static String toString(JsonValue value, boolean nullok) {
    // check what type this is
    if (value.isString())
      return value.asString();
    else if (value.isNull() && nullok)
      return null;

    // not sure how well this works in practice, but let's try
    return value.toString();
  }

  public static JsonValue toArray(JsonValue value, boolean nullok) {
    // check what type this is
    if (value.isArray())
      return value;
    else if (value.isNull() && nullok)
      return null;

    throw new JsltException("Cannot convert " + value + " to array");
  }

  public static JsonValue number(JsonValue value, Location loc) {
    return number(value, false, loc);
  }

  public static JsonValue number(JsonValue value, boolean strict, Location loc) {
    // this works, because Java null can never be a function parameter
    // in JSLT, unlike JSON null
    return number(value, strict, loc, null);
  }

  public static JsonValue number(JsonValue value, boolean strict, Location loc,
                                JsonValue fallback) {
    // check what type this is
    if (value.isNumber())
      return value;
    else if (value.isNull()) {
      if (fallback == null)
        return value;
      else
        return fallback;
    } else if (!value.isString()) {
      if (strict)
        throw new JsltException("Can't convert " + value + " to number", loc);
      else if (fallback == null)
        return value.makeNull();
      else
        return fallback;
    }

    // let's look at this number
    String number = value.asString();
    JsonValue numberNode = parseNumber(number);
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
  private static JsonValue parseNumber(String number) {
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
      if (number.length() < 19)
        return new LongJValue(Long.parseLong(number));
      else
        return new BigIntegerJValue(new BigInteger(number));
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
        return new DoubleJValue(value);
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
    return new DoubleJValue(value * Math.pow(10, exponent));
  }

  private static int scanDigits(String number, int pos) {
    while (pos < number.length() && isDigit(number.charAt(pos)))
      pos++;
    return pos;
  }

  private static boolean isDigit(char ch) {
    return ch >= '0' && ch <= '9';
  }

  public static JsonValue convertObjectToArray(JsonValue object) {
    int ix = 0;
    JsonValue[] buffer = new JsonValue[object.size()];
    Iterator<String> it = object.getKeys();
    while (it.hasNext()) {
      String key = it.next();
      buffer[ix++] = object.makeObjectBuilder()
        .set("key", object.makeValue(key))
        .set("value", object.get(key))
        .build();
    }
    return object.makeArray(buffer);
  }

  public static String indent(int level) {
    char[] indent = new char[level * 2];
    for (int ix = 0; ix < indent.length; ix++)
      indent[ix] = ' ';
    return new String(indent, 0, indent.length);
  }
}
