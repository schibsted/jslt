
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.schibsted.spt.data.json.*;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;

/**
 * For now contains all the various function implementations. Should
 * probably be broken up into separate files and use annotations to
 * capture a lot of this information instead.
 */
public class BuiltinFunctions {

  // this will be replaced with a proper Context. need to figure out
  // relationship between compile-time and run-time context first.
  public static Map<String, Function> functions = new HashMap();
  static {
    // GENERAL
    functions.put("contains", new BuiltinFunctions.Contains());
    functions.put("size", new BuiltinFunctions.Size());
    functions.put("error", new BuiltinFunctions.Error());
    functions.put("min", new BuiltinFunctions.Min());
    functions.put("max", new BuiltinFunctions.Max());

    // NUMERIC
    functions.put("is-number", new BuiltinFunctions.IsNumber());
    functions.put("is-integer", new BuiltinFunctions.IsInteger());
    functions.put("is-decimal", new BuiltinFunctions.IsDecimal());
    functions.put("number", new BuiltinFunctions.Number());
    functions.put("round", new BuiltinFunctions.Round());
    functions.put("floor", new BuiltinFunctions.Floor());
    functions.put("ceiling", new BuiltinFunctions.Ceiling());
    functions.put("random", new BuiltinFunctions.Random());
    functions.put("sum", new BuiltinFunctions.Sum());
    functions.put("mod", new BuiltinFunctions.Modulo());
    functions.put("hash-int", new BuiltinFunctions.HashInt());

    // STRING
    functions.put("is-string", new BuiltinFunctions.IsString());
    functions.put("string", new BuiltinFunctions.ToString());
    functions.put("test", new BuiltinFunctions.Test());
    functions.put("capture", new BuiltinFunctions.Capture());
    functions.put("split", new BuiltinFunctions.Split());
    functions.put("join", new BuiltinFunctions.Join());
    functions.put("lowercase", new BuiltinFunctions.Lowercase());
    functions.put("uppercase", new BuiltinFunctions.Uppercase());
    functions.put("sha256-hex", new BuiltinFunctions.Sha256());
    functions.put("starts-with", new BuiltinFunctions.StartsWith());
    functions.put("ends-with", new BuiltinFunctions.EndsWith());
    functions.put("from-json", new BuiltinFunctions.FromJson());
    functions.put("to-json", new BuiltinFunctions.ToJson());
    functions.put("replace", new BuiltinFunctions.Replace());
    functions.put("trim", new BuiltinFunctions.Trim());

    // BOOLEAN
    functions.put("not", new BuiltinFunctions.Not());
    functions.put("boolean", new BuiltinFunctions.Boolean());
    functions.put("is-boolean", new BuiltinFunctions.IsBoolean());

    // OBJECT
    functions.put("is-object", new BuiltinFunctions.IsObject());
    functions.put("get-key", new BuiltinFunctions.GetKey());

    // ARRAY
    functions.put("array", new BuiltinFunctions.Array());
    functions.put("is-array", new BuiltinFunctions.IsArray());
    functions.put("flatten", new BuiltinFunctions.Flatten());
    functions.put("all", new BuiltinFunctions.All());
    functions.put("any", new BuiltinFunctions.Any());
    functions.put("zip", new BuiltinFunctions.Zip());
    functions.put("zip-with-index", new BuiltinFunctions.ZipWithIndex());
    functions.put("index-of", new BuiltinFunctions.IndexOf());

    // TIME
    functions.put("now", new BuiltinFunctions.Now());
    functions.put("parse-time", new BuiltinFunctions.ParseTime());
    functions.put("format-time", new BuiltinFunctions.FormatTime());

    // MISC
    functions.put("parse-url", new BuiltinFunctions.ParseUrl());
  }

  public static Map<String, Macro> macros = new HashMap();
  static {
    macros.put("fallback", new BuiltinFunctions.Fallback());
  }

  private static abstract class AbstractMacro extends AbstractCallable implements Macro {

    public AbstractMacro(String name, int min, int max) {
      super(name, min, max);
    }
  }

  // ===== NUMBER

  public static class Number extends AbstractFunction {

    public Number() {
      super("number", 1, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      if (arguments.length == 1)
        return NodeUtils.number(arguments[0], true, null);
      else
        return NodeUtils.number(arguments[0], false, null, arguments[1]);
    }
  }

  // ===== ROUND

  public static class Round extends AbstractFunction {

    public Round() {
      super("round", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue number = arguments[0];
      if (number.isNull())
        return number;
      else if (!number.isNumber())
        throw new JsltException("round() cannot round a non-number: " + number);

      return input.makeValue(Math.round(number.asDouble()));
    }
  }

  // ===== FLOOR

  public static class Floor extends AbstractFunction {

    public Floor() {
      super("floor", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue number = arguments[0];
      if (number.isNull())
        return number;
      else if (!number.isNumber())
        throw new JsltException("floor() cannot round a non-number: " + number);

      return input.makeValue((long) Math.floor(number.asDouble()));
    }
  }

  // ===== CEILING

  public static class Ceiling extends AbstractFunction {

    public Ceiling() {
      super("ceiling", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue number = arguments[0];
      if (number.isNull())
        return number;
      else if (!number.isNumber())
        throw new JsltException("ceiling() cannot round a non-number: " + number);

      return input.makeValue((long) Math.ceil(number.asDouble()));
    }
  }

  // ===== RANDOM

  public static class Random extends AbstractFunction {
    private static java.util.Random random = new java.util.Random();

    public Random() {
      super("random", 0, 0);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(random.nextDouble());
    }
  }

  // ===== SUM

  public static class Sum extends AbstractFunction {

    public Sum() {
      super("sum", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue array = arguments[0];
      if (array.isNull())
        return array;
      else if (!array.isArray())
        throw new JsltException("sum(): argument must be array, was " + array);

      double sum = 0.0;
      boolean integral = true;
      for (int ix = 0; ix < array.size(); ix++) {
        JsonValue value = array.get(ix);
        if (!value.isNumber())
          throw new JsltException("sum(): array must contain numbers, found " + value);
        integral &= value.isIntegralNumber();

        sum += value.asDouble();
      }
      if (integral)
        return input.makeValue((long) sum);
      else
        return input.makeValue(sum);
    }
  }

  // ===== MODULO

  public static class Modulo extends AbstractFunction {

    public Modulo() {
      super("modulo", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue dividend = arguments[0];
      if (dividend.isNull())
        return input.makeNull();
      else if (!dividend.isNumber())
        throw new JsltException("mod(): dividend cannot be a non-number: " + dividend);

      JsonValue divisor = arguments[1];
      if (divisor.isNull())
        return input.makeNull();
      else if (!divisor.isNumber())
        throw new JsltException("mod(): divisor cannot be a non-number: " + divisor);

      if (!dividend.isIntegralNumber() || !divisor.isIntegralNumber()) {
        throw new JsltException("mod(): operands must be integral types");
      } else {
        long D = dividend.asLong();
        long d = divisor.asLong();
        if (d == 0)
          throw new JsltException("mod(): cannot divide by zero");

        long r = D % d;
        if (r < 0) {
          if (d > 0)
            r += d;
          else
            r -= d;
        }

        return input.makeValue(r);
      }
    }
  }

  // ===== HASH-INT

  public static class HashInt extends AbstractFunction {

    public HashInt() {
      super("hash-int", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue node = arguments[0];
      if (node.isNull())
        return node;

      String jsonString = JsonIO.toStringWithSortedKeys(node);
      return input.makeValue(jsonString.hashCode());
    }
  }

  // ===== TEST

  public static class Test extends AbstractFunction {
    public Test() {
      super("test", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // if data is missing then it doesn't match, end of story
      if (arguments[0].isNull())
        return input.makeFalse();

      String string = NodeUtils.toString(arguments[0], false);
      String regexp = NodeUtils.toString(arguments[1], true);
      if (regexp == null)
        throw new JsltException("test() can't test null regexp");

      Pattern p = getRegexp(regexp);
      java.util.regex.Matcher m = p.matcher(string);
      return input.makeValue(m.find(0));
    }
  }

  // ===== CAPTURE

  // believe it or not, but the built-in Java regex library is so
  // incredibly shitty that it doesn't allow you to learn what the
  // names of the named groups are. so we have to use regexps to
  // parse the regexps. (lots of swearing omitted.)

  public static class Capture extends AbstractFunction {
    static Map<String, JstlPattern> cache = new BoundedCache(1000);

    public Capture() {
      super("capture", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // if data is missing then it doesn't match, end of story
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      String regexps = NodeUtils.toString(arguments[1], true);
      if (regexps == null)
        throw new JsltException("capture() can't match against null regexp");

      JstlPattern regex = cache.get(regexps);
      if (regex == null) {
        regex = new JstlPattern(regexps);
        cache.put(regexps, regex);
      }

      JsonObjectBuilder node = input.makeObjectBuilder();
      Matcher m = regex.matcher(string);
      if (m.find()) {
        for (String group : regex.getGroups()) {
          try {
            node.set(group, m.group(group));
          } catch (IllegalStateException e) {
            // this group had no match: do nothing
          }
        }
      }

      return node.build();
    }
  }

  // from https://stackoverflow.com/a/15588989/5974641
  private static class JstlPattern {
    private Pattern pattern;
    private Set<String> groups;

    public JstlPattern(String regexp) {
      this.pattern = Pattern.compile(regexp);
      this.groups = getNamedGroups(regexp);
    }

    public Matcher matcher(String input) {
      return pattern.matcher(input);
    }

    public Set<String> getGroups() {
      return groups;
    }

    private static Pattern extractor =
      Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    private static Set<String> getNamedGroups(String regex) {
      Set<String> groups = new TreeSet<String>();

      Matcher m = extractor.matcher(regex);
      while (m.find())
        groups.add(m.group(1));

      return groups;
    }
  }

  // ===== SPLIT

  public static class Split extends AbstractFunction {

    public Split() {
      super("split", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      String split = NodeUtils.toString(arguments[1], true);
      if (split == null)
        throw new JsltException("split() can't split on null");

      String[] parts = string.split(split);
      JsonValue[] buffer = new JsonValue[parts.length];
      for (int ix = 0; ix < parts.length; ix++)
        buffer[ix] = input.makeValue(parts[ix]);
      return input.makeArray(buffer);
    }
  }

  // ===== LOWERCASE

  public static class Lowercase extends AbstractFunction {

    public Lowercase() {
      super("lowercase", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      return input.makeValue(string.toLowerCase());
    }
  }

  // ===== UPPERCASE

  public static class Uppercase extends AbstractFunction {

    public Uppercase() {
      super("uppercase", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      return input.makeValue(string.toUpperCase());
    }
  }


  // ===== SHA256

  public static class Sha256 extends AbstractFunction {
    final MessageDigest messageDigest;

    public Sha256() {
      super("sha256-hex", 1, 1);
      try {
        messageDigest = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e) {
        throw new JsltException("sha256-hex: could not find sha256 algorithm " + e);
      }
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String message = NodeUtils.toString(arguments[0], false);

      byte[] bytes = this.messageDigest.digest(message.getBytes(UTF_8));
      String string = Utils.printHexBinary(bytes);

      return input.makeValue(string);
    }
  }

  // ===== NOT

  public static class Not extends AbstractFunction {

    public Not() {
      super("not", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(!NodeUtils.isTrue(arguments[0]));
    }
  }

  // ===== BOOLEAN

  public static class Boolean extends AbstractFunction {

    public Boolean() {
      super("boolean", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(NodeUtils.isTrue(arguments[0]));
    }
  }

  // ===== IS-BOOLEAN

  public static class IsBoolean extends AbstractFunction {

    public IsBoolean() {
      super("is-boolean", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isBoolean());
    }
  }

  // ===== FALLBACK

  public static class Fallback extends AbstractMacro {

    public Fallback() {
      super("fallback", 2, 1024);
    }

    public JsonValue call(Scope scope, JsonValue input,
                         ExpressionNode[] parameters) {
      // making this a macro means we can evaluate only the parameters
      // that are necessary to find a value, and leave the rest
      // untouched, giving better performance

      for (int ix = 0; ix < parameters.length; ix++) {
        JsonValue value = parameters[ix].apply(scope, input);
        if (NodeUtils.isValue(value))
          return value;
      }
      return input.makeNull();
    }
  }

  // ===== IS-OBJECT

  public static class IsObject extends AbstractFunction {

    public IsObject() {
      super("is-object", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isObject());
    }
  }

  // ===== GET-KEY

  public static class GetKey extends AbstractFunction {

    public GetKey() {
      super("get-key", 2, 3);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String key = NodeUtils.toString(arguments[1], true);
      if (key == null)
        return input.makeNull();

      JsonValue obj = arguments[0];
      if (obj.isObject()) {
        JsonValue value = obj.get(key);
        if (value == null) {
          if (arguments.length == 2)
            return input.makeNull();
          else
            return arguments[2]; // fallback argument
        } else
          return value;
      } else if (obj.isNull())
        return input.makeNull();
      else
        throw new JsltException("get-key: can't look up keys in " + obj);
    }
  }

  // ===== IS-ARRAY

  public static class IsArray extends AbstractFunction {

    public IsArray() {
      super("is-array", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isArray());
    }
  }

  // ===== ARRAY

  public static class Array extends AbstractFunction {

    public Array() {
      super("array", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue value = arguments[0];
      if (value.isNull() || value.isArray())
        return value;
      else if (value.isObject())
        return NodeUtils.convertObjectToArray(value);
      else
        throw new JsltException("array() cannot convert " + value);
    }
  }

  // ===== FLATTEN

  public static class Flatten extends AbstractFunction {

    public Flatten() {
      super("flatten", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue value = arguments[0];
      if (value.isNull())
        return value;
      else if (!value.isArray())
        throw new JsltException("flatten() cannot operate on " + value);

      // FIXME: how to do this efficiently?
      // ArrayNode array = NodeUtils.mapper.createArrayNode();
      // flatten(array, value);
      // return array;
      return null;
    }

    private void flatten(JsonValue array, JsonValue current) {
      for (int ix = 0; ix < current.size(); ix++) {
        JsonValue node = current.get(ix);
        if (node.isArray())
          flatten(array, node);

        // FIXME
        // else
        //   array.add(node);
      }
    }
  }

  // ===== ALL

  public static class All extends AbstractFunction {

    public All() {
      super("all", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue value = arguments[0];
      if (value.isNull())
        return value;
      else if (!value.isArray())
        throw new JsltException("all() requires an array, not " + value);

      for (int ix = 0; ix < value.size(); ix++) {
        JsonValue node = value.get(ix);
        if (!NodeUtils.isTrue(node))
          return input.makeFalse();
      }
      return input.makeTrue();
    }

  }

  // ===== ANY

  public static class Any extends AbstractFunction {

    public Any() {
      super("any", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue value = arguments[0];
      if (value.isNull())
        return value;
      else if (!value.isArray())
        throw new JsltException("any() requires an array, not " + value);

      for (int ix = 0; ix < value.size(); ix++) {
        JsonValue node = value.get(ix);
        if (NodeUtils.isTrue(node))
          return input.makeTrue();
      }
      return input.makeFalse();
    }

  }

  // ===== ZIP

  public static class Zip extends AbstractFunction {

    public Zip() {
      super("zip", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue array1 = arguments[0];
      JsonValue array2 = arguments[1];
      if (array1.isNull() || array2.isNull())
        return input.makeNull();
      else if (!array1.isArray() || !array2.isArray())
        throw new JsltException("zip() requires arrays");
      else if (array1.size() != array2.size())
        throw new JsltException("zip() arrays were of unequal size");

      JsonValue[] buffer = new JsonValue[array1.size()];
      for (int ix = 0; ix < array1.size(); ix++) {
        JsonValue[] pair = new JsonValue[] {
          array1.get(ix), array2.get(ix)
        };
        buffer[ix] = input.makeArray(pair);
      }
      return input.makeArray(buffer);
    }

  }

  // ===== ZIP-WITH-INDEX

  public static class ZipWithIndex extends AbstractFunction {

    public ZipWithIndex() {
      super("zip-with-index", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue arrayIn = arguments[0];
      if (arrayIn.isNull())
        return input.makeNull();
      else if (!arrayIn.isArray())
        throw new JsltException("zip-with-index() argument must be an array");

      JsonValue[] buffer = new JsonValue[arrayIn.size()];
      for (int ix = 0; ix < arrayIn.size(); ix++) {
        buffer[ix] = input.makeObjectBuilder()
          .set("index", input.makeValue(ix))
          .set("value", arrayIn.get(ix))
          .build();
      }
      return input.makeArray(buffer);
    }

  }

  // ===== INDEX-OF

  public static class IndexOf extends AbstractFunction {

    public IndexOf() {
      super("index-of", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue array = arguments[0];
      if (array.isNull())
        return input.makeNull();
      else if (!array.isArray())
        throw new JsltException("index-of() first argument must be an array");

      JsonValue value = arguments[1];
      for (int ix = 0; ix < array.size(); ix++) {
        if (EqualsComparison.equals(array.get(ix), value))
          return input.makeValue(ix);
      }
      return input.makeValue(-1);
    }

  }

  // ===== STARTS-WITH

  public static class StartsWith extends AbstractFunction {

    public StartsWith() {
      super("starts-with", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String string = NodeUtils.toString(arguments[0], false);
      String prefix = NodeUtils.toString(arguments[1], false);
      return input.makeValue(string.startsWith(prefix));
    }
  }

  // ===== ENDS-WITH

  public static class EndsWith extends AbstractFunction {

    public EndsWith() {
      super("ends-with", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String string = NodeUtils.toString(arguments[0], false);
      String suffix = NodeUtils.toString(arguments[1], false);
      return input.makeValue(string.endsWith(suffix));
    }
  }

  // ===== FROM-JSON

  public static class FromJson extends AbstractFunction {

    public FromJson() {
      super("from-json", 1, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String json = NodeUtils.toString(arguments[0], true);
      if (json == null)
        return input.makeNull();

      try {
        return JsonIO.parseString(json);
      } catch (JsltException e) {
        if (arguments.length == 2)
          return arguments[1]; // return fallback on parse fail
        else
          throw new JsltException("from-json can't parse " + json + ": " + e);
      }
    }
  }

  // ===== TO-JSON

  public static class ToJson extends AbstractFunction {

    public ToJson() {
      super("to-json", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(JsonIO.toString(arguments[0]));
    }
  }

  // ===== REPLACE

  public static class Replace extends AbstractFunction {

    public Replace() {
      super("replace", 3, 3);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String string = NodeUtils.toString(arguments[0], true);
      if (string == null)
        return input.makeNull();

      String regexp = NodeUtils.toString(arguments[1], false);
      String sep = NodeUtils.toString(arguments[2], false);

      Pattern p = getRegexp(regexp);
      Matcher m = p.matcher(string);
      char[] buf = new char[string.length() * Math.max(sep.length(), 1)];
      int pos = 0; // next untouched character in input
      int bufix = 0; // next unwritten character in buf

      while (m.find(pos)) {
        // we found another match, and now matcher state has been updated
        if (m.start() == m.end())
          throw new JsltException("Regexp " + regexp + " in replace() matched empty string in '" + arguments[0] + "'");

        // if there was text between pos and start of match, copy to output
        if (pos < m.start())
          bufix = copy(string, buf, bufix, pos, m.start());

        // copy sep to output (corresponds with the match)
        bufix = copy(sep, buf, bufix, 0, sep.length());

        // step over match
        pos = m.end();
      }

      if (pos == 0 && arguments[0].isString())
        // there were matches, so the string hasn't changed
        return arguments[0];
      else if (pos < string.length())
        // there was text remaining after the end of the last match. must copy
        bufix = copy(string, buf, bufix, pos, string.length());

      return input.makeValue(new String(buf, 0, bufix));
    }
  }

  // ===== TRIM

  public static class Trim extends AbstractFunction {

    public Trim() {
      super("trim", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String string = NodeUtils.toString(arguments[0], true);
      if (string == null)
        return input.makeNull();

      return input.makeValue(string.trim());
    }
  }

  // ===== JOIN

  public static class Join extends AbstractFunction {

    public Join() {
      super("join", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue array = NodeUtils.toArray(arguments[0], true);
      if (array == null)
        return input.makeNull();

      String sep = NodeUtils.toString(arguments[1], false);

      StringBuilder buf = new StringBuilder();
      for (int ix = 0; ix < array.size(); ix++) {
        if (ix > 0)
          buf.append(sep);
        buf.append(NodeUtils.toString(array.get(ix), false));
      }
      return input.makeValue(buf.toString());
    }
  }

  // ===== CONTAINS

  public static class Contains extends AbstractFunction {

    public Contains() {
      super("contains", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      if (arguments[1].isNull())
        return input.makeFalse(); // nothing is contained in null

      else if (arguments[1].isArray()) {
        for (int ix = 0; ix < arguments[1].size(); ix++)
          if (arguments[1].get(ix).equals(arguments[0]))
            return input.makeTrue();

      } else if (arguments[1].isObject()) {
        String key = NodeUtils.toString(arguments[0], true);
        if (key == null)
          return input.makeFalse();

        return input.makeValue(arguments[1].has(key));

      } else if (arguments[1].isString()) {
        String sub = NodeUtils.toString(arguments[0], true);
        if (sub == null)
          return input.makeFalse();

        String str = arguments[1].asString();
        return input.makeValue(str.indexOf(sub) != -1);

      } else
        throw new JsltException("Contains cannot operate on " + arguments[1]);

      return input.makeFalse();
    }
  }

  // ===== SIZE

  public static class Size extends AbstractFunction {

    public Size() {
      super("size", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      if (arguments[0].isArray() || arguments[0].isObject() ||
          arguments[0].isString())
        return input.makeValue(arguments[0].size());

      else if (arguments[0].isNull())
        return arguments[0];

      else
        throw new JsltException("Function size() cannot work on " + arguments[0]);
    }
  }

  // ===== ERROR

  public static class Error extends AbstractFunction {

    public Error() {
      super("error", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String msg = NodeUtils.toString(arguments[0], false);
      throw new JsltException("error: " + msg);
    }
  }

  // ===== STRING

  public static class ToString extends AbstractFunction {

    public ToString() {
      super("string", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      if (arguments[0].isString())
        return arguments[0];
      else
        return input.makeValue(arguments[0].toString());
    }
  }

  // ===== IS-STRING

  public static class IsString extends AbstractFunction {

    public IsString() {
      super("is-string", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isString());
    }
  }

  // ===== IS-NUMBER

  public static class IsNumber extends AbstractFunction {

    public IsNumber() {
      super("is-number", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isNumber());
    }
  }

  // ===== IS-INTEGER

  public static class IsInteger extends AbstractFunction {

    public IsInteger() {
      super("is-integer", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isIntegralNumber());
    }
  }

  // ===== IS-DECIMAL

  public static class IsDecimal extends AbstractFunction {

    public IsDecimal() {
      super("is-decimal", 1, 1);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      return input.makeValue(arguments[0].isDecimalNumber());
    }
  }

  // ===== NOW

  public static class Now extends AbstractFunction {

    public Now() {
      super("now", 0, 0);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      long ms = System.currentTimeMillis();
      return input.makeValue( ms / 1000.0 );
    }
  }

  // ===== PARSE-TIME

  public static class ParseTime extends AbstractFunction {

    public ParseTime() {
      super("parse-time", 2, 3);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      String text = NodeUtils.toString(arguments[0], true);
      if (text == null)
        return input.makeNull();

      String formatstr = NodeUtils.toString(arguments[1], false);
      JsonValue fallback = null;
      if (arguments.length > 2)
        fallback = arguments[2];

      // the performance of this could be better, but it's not so easy
      // to fix that when SimpleDateFormat isn't thread-safe, so we
      // can't safely share it between threads

      try {
        SimpleDateFormat format = new SimpleDateFormat(formatstr);
        format.setTimeZone(new SimpleTimeZone(0, "UTC"));
        Date time = format.parse(text);
        return input.makeValue((double) (time.getTime() / 1000.0));
      } catch (IllegalArgumentException e) {
        // thrown if format is bad
        throw new JsltException("parse-time: Couldn't parse format '" + formatstr + "': " + e.getMessage());
      } catch (ParseException e) {
        if (fallback == null)
          throw new JsltException("parse-time: " + e.getMessage());
        else
          return fallback;
      }
    }
  }

  // ===== FORMAT-TIME

  public static class FormatTime extends AbstractFunction {
    static Set<String> zonenames = new HashSet();
    static {
      zonenames.addAll(Arrays.asList(TimeZone.getAvailableIDs()));
    }

    public FormatTime() {
      super("format-time", 2, 3);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      JsonValue number = NodeUtils.number(arguments[0], null);
      if (number == null || number.isNull())
        return input.makeNull();

      double timestamp = number.asDouble();

      String formatstr = NodeUtils.toString(arguments[1], false);

      TimeZone zone = new SimpleTimeZone(0, "UTC");
      if (arguments.length == 3) {
        String zonename = NodeUtils.toString(arguments[2], false);
        if (!zonenames.contains(zonename))
          throw new JsltException("format-time: Unknown timezone " + zonename);
        zone = TimeZone.getTimeZone(zonename);
      }

      // the performance of this could be better, but it's not so easy
      // to fix that when SimpleDateFormat isn't thread-safe, so we
      // can't safely share it between threads

      try {
        SimpleDateFormat format = new SimpleDateFormat(formatstr);
        format.setTimeZone(zone);
        String formatted = format.format(Math.round(timestamp * 1000));
        return input.makeValue(formatted);
      } catch (IllegalArgumentException e) {
        // thrown if format is bad
        throw new JsltException("format-time: Couldn't parse format '" + formatstr + "': " + e.getMessage());
      }
    }
  }

  // ===== MIN

  public static class Min extends AbstractFunction {
    public Min() {
      super("min", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      // this works because null is the smallest of all values
      if (ComparisonOperator.compare(arguments[0], arguments[1], null) < 0)
        return arguments[0];
      else
        return arguments[1];
    }
  }

  // ===== MAX

  public static class Max extends AbstractFunction {
    public Max() {
      super("max", 2, 2);
    }

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      if (arguments[0].isNull() || arguments[1].isNull())
        return input.makeNull();
      else if (ComparisonOperator.compare(arguments[0], arguments[1], null) > 0)
        return arguments[0];
      else
        return arguments[1];
    }
  }

  // ===== PARSE-URL

  public static class ParseUrl extends AbstractFunction {
    public ParseUrl() { super("parse-url", 1,1);}

    public JsonValue call(JsonValue input, JsonValue[] arguments) {
      if (arguments[0].isNull())
        return input.makeNull();

      String urlString = arguments[0].asString();

      try {
        URL aURL = new URL(arguments[0].asString());
        JsonObjectBuilder objectNode = input.makeObjectBuilder();
        if (aURL.getHost() != null && !aURL.getHost().isEmpty())
          objectNode.set("host", aURL.getHost());
        if (aURL.getPort() != -1)
          objectNode.set("port", input.makeValue(aURL.getPort()));
        if (!aURL.getPath().isEmpty())
          objectNode.set("path", aURL.getPath());
        if (aURL.getProtocol() != null && !aURL.getProtocol().isEmpty())
          objectNode.set("scheme", aURL.getProtocol());
        if (aURL.getQuery() != null && !aURL.getQuery().isEmpty()) {
          objectNode.set("query", aURL.getQuery());
          JsonObjectBuilder queryParamsNode = input.makeObjectBuilder();
          objectNode.set("parameters", queryParamsNode.build());
          String[] pairs = aURL.getQuery().split("&");
          for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            // if (!queryParamsNode.has(key)) queryParamsNode.set(key, NodeUtils.mapper.createArrayNode());
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            //JsonValue valuesNode = queryParamsNode.get(key);
            //valuesNode.add(value); // FIXME: no can do
          }
        }
        if(aURL.getRef() != null)
          objectNode.set("fragment", aURL.getRef());
        if(aURL.getUserInfo() != null && !aURL.getUserInfo().isEmpty())
          objectNode.set("userinfo", aURL.getUserInfo());
        return objectNode.build();
      } catch (MalformedURLException | UnsupportedEncodingException e) {
        throw new JsltException("Can't parse " + urlString, e);
      }
    }
  }

  // ===== HELPER METHODS

  // shared regexp cache
  static Map<String, Pattern> cache = new BoundedCache(1000);

  private synchronized static Pattern getRegexp(String regexp) {
    Pattern p = cache.get(regexp);
    if (p == null) {
      p = Pattern.compile(regexp);
      cache.put(regexp, p);
    }
    return p;
  }

  private static int copy(String input, char[] buf, int bufix,
                          int from, int to) {
    for (int ix = from; ix < to; ix++)
      buf[bufix++] = input.charAt(ix);
    return bufix;
  }
}
