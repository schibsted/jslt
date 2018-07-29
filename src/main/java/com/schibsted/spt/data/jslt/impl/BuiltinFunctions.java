
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

import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.DoubleNode;

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

    // NUMERIC
    functions.put("is-number", new BuiltinFunctions.IsNumber());
    functions.put("number", new BuiltinFunctions.Number());
    functions.put("round", new BuiltinFunctions.Round());
    functions.put("floor", new BuiltinFunctions.Floor());
    functions.put("ceiling", new BuiltinFunctions.Ceiling());
    functions.put("random", new BuiltinFunctions.Random());

    // STRING
    functions.put("is-string", new BuiltinFunctions.IsString());
    functions.put("string", new BuiltinFunctions.ToString());
    functions.put("test", new BuiltinFunctions.Test());
    functions.put("capture", new BuiltinFunctions.Capture());
    functions.put("split", new BuiltinFunctions.Split());
    functions.put("join", new BuiltinFunctions.Join());
    functions.put("lowercase", new BuiltinFunctions.Lowercase());
    functions.put("uppercase", new BuiltinFunctions.Uppercase());
    functions.put("starts-with", new BuiltinFunctions.StartsWith());
    functions.put("ends-with", new BuiltinFunctions.EndsWith());
    functions.put("from-json", new BuiltinFunctions.FromJson());
    functions.put("to-json", new BuiltinFunctions.ToJson());

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

    // TIME
    functions.put("now", new BuiltinFunctions.Now());
    functions.put("parse-time", new BuiltinFunctions.ParseTime());
    functions.put("format-time", new BuiltinFunctions.FormatTime());
  }

  public static Map<String, Macro> macros = new HashMap();
  static {
    macros.put("fallback", new BuiltinFunctions.Fallback());
  }

  private static abstract class AbstractCallable implements Callable {
    private String name;
    private int min;
    private int max;

    public AbstractCallable(String name, int min, int max) {
      this.name = name;
      this.min = min;
      this.max = max;
    }

    public String getName() {
      return name;
    }

    public int getMinArguments() {
      return min;
    }

    public int getMaxArguments() {
      return max;
    }
  }

  private static abstract class AbstractFunction extends AbstractCallable implements Function {

    public AbstractFunction(String name, int min, int max) {
      super(name, min, max);
    }
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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      if (arguments.length == 1)
        return NodeUtils.number(arguments[0], true, null);
      else
        return NodeUtils.number(arguments[0], true, null, arguments[1]);
    }
  }

  // ===== ROUND

  public static class Round extends AbstractFunction {

    public Round() {
      super("round", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode number = arguments[0];
      if (number.isNull())
        return NullNode.instance;
      else if (!number.isNumber())
        throw new JsltException("round() cannot round a non-number: " + number);

      return new LongNode(Math.round(number.doubleValue()));
    }
  }

  // ===== FLOOR

  public static class Floor extends AbstractFunction {

    public Floor() {
      super("floor", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode number = arguments[0];
      if (number.isNull())
        return NullNode.instance;
      else if (!number.isNumber())
        throw new JsltException("floor() cannot round a non-number: " + number);

      return new LongNode((long) Math.floor(number.doubleValue()));
    }
  }

  // ===== CEILING

  public static class Ceiling extends AbstractFunction {

    public Ceiling() {
      super("ceiling", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode number = arguments[0];
      if (number.isNull())
        return NullNode.instance;
      else if (!number.isNumber())
        throw new JsltException("ceiling() cannot round a non-number: " + number);

      return new LongNode((long) Math.ceil(number.doubleValue()));
    }
  }

  // ===== RANDOM

  public static class Random extends AbstractFunction {
    private static java.util.Random random = new java.util.Random();

    public Random() {
      super("random", 0, 0);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return new DoubleNode(random.nextDouble());
    }
  }

  // ===== TEST

  public static class Test extends AbstractFunction {
    static Map<String, Pattern> cache = new BoundedCache(1000);

    public Test() {
      super("test", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      // if data is missing then it doesn't match, end of story
      if (arguments[0].isNull())
        return BooleanNode.FALSE;

      String string = NodeUtils.toString(arguments[0], false);
      String regexp = NodeUtils.toString(arguments[1], true);
      if (regexp == null)
        throw new JsltException("test() can't test null regexp");

      Pattern p = cache.get(regexp);
      if (p == null) {
        p = Pattern.compile(regexp);
        cache.put(regexp, p);
      }
      java.util.regex.Matcher m = p.matcher(string);
      return NodeUtils.toJson(m.find(0));
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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
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

      ObjectNode node = NodeUtils.mapper.createObjectNode();
      Matcher m = regex.matcher(string);
      if (m.find()) {
        for (String group : regex.getGroups()) {
          try {
            node.put(group, m.group(group));
          } catch (IllegalStateException e) {
            // this group had no match: do nothing
          }
        }
      }

      return node;
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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      String split = NodeUtils.toString(arguments[1], true);
      if (split == null)
        throw new JsltException("split() can't split on null");

      return NodeUtils.toJson(string.split(split));
    }
  }

  // ===== LOWERCASE

  public static class Lowercase extends AbstractFunction {

    public Lowercase() {
      super("lowercase", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      return new TextNode(string.toLowerCase());
    }
  }

  // ===== UPPERCASE

  public static class Uppercase extends AbstractFunction {

    public Uppercase() {
      super("uppercase", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      // if input string is missing then we're doing nothing
      if (arguments[0].isNull())
        return arguments[0]; // null

      String string = NodeUtils.toString(arguments[0], false);
      return new TextNode(string.toUpperCase());
    }
  }

  // ===== NOT

  public static class Not extends AbstractFunction {

    public Not() {
      super("not", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(!NodeUtils.isTrue(arguments[0]));
    }
  }

  // ===== BOOLEAN

  public static class Boolean extends AbstractFunction {

    public Boolean() {
      super("boolean", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(NodeUtils.isTrue(arguments[0]));
    }
  }

  // ===== IS-BOOLEAN

  public static class IsBoolean extends AbstractFunction {

    public IsBoolean() {
      super("is-boolean", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(arguments[0].isBoolean());
    }
  }

  // ===== FALLBACK

  public static class Fallback extends AbstractMacro {

    public Fallback() {
      super("fallback", 2, 1024);
    }

    public JsonNode call(Scope scope, JsonNode input,
                         ExpressionNode[] parameters) {
      // making this a macro means we can evaluate only the parameters
      // that are necessary to find a value, and leave the rest
      // untouched, giving better performance

      for (int ix = 0; ix < parameters.length; ix++) {
        JsonNode value = parameters[ix].apply(scope, input);
        if (NodeUtils.isValue(value))
          return value;
      }
      return NullNode.instance;
    }
  }

  // ===== IS-OBJECT

  public static class IsObject extends AbstractFunction {

    public IsObject() {
      super("is-object", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(arguments[0].isObject());
    }
  }

  // ===== GET-KEY

  public static class GetKey extends AbstractFunction {

    public GetKey() {
      super("get-key", 2, 3);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      String key = NodeUtils.toString(arguments[1], true);
      if (key == null)
        return NullNode.instance;

      JsonNode obj = arguments[0];
      if (obj.isObject()) {
        JsonNode value = obj.get(key);
        if (value == null) {
          if (arguments.length == 2)
            return NullNode.instance;
          else
            return arguments[2]; // fallback argument
        } else
          return value;
      } else if (obj.isNull())
        return NullNode.instance;
      else
        throw new JsltException("get-key: can't look up keys in " + obj);
    }
  }

  // ===== IS-ARRAY

  public static class IsArray extends AbstractFunction {

    public IsArray() {
      super("is-array", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(arguments[0].isArray());
    }
  }

  // ===== ARRAY

  public static class Array extends AbstractFunction {

    public Array() {
      super("array", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode value = arguments[0];
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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode value = arguments[0];
      if (value.isNull())
        return value;
      else if (!value.isArray())
        throw new JsltException("flatten() cannot operate on " + value);

      ArrayNode array = NodeUtils.mapper.createArrayNode();
      flatten(array, value);
      return array;
    }

    private void flatten(ArrayNode array, JsonNode current) {
      for (int ix = 0; ix < current.size(); ix++) {
        JsonNode node = current.get(ix);
        if (node.isArray())
          flatten(array, node);
        else
          array.add(node);
      }
    }
  }

  // ===== STARTS-WITH

  public static class StartsWith extends AbstractFunction {

    public StartsWith() {
      super("starts-with", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      String string = NodeUtils.toString(arguments[0], false);
      String prefix = NodeUtils.toString(arguments[1], false);
      return NodeUtils.toJson(string.startsWith(prefix));
    }
  }

  // ===== ENDS-WITH

  public static class EndsWith extends AbstractFunction {

    public EndsWith() {
      super("ends-with", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      String string = NodeUtils.toString(arguments[0], false);
      String suffix = NodeUtils.toString(arguments[1], false);
      return NodeUtils.toJson(string.endsWith(suffix));
    }
  }

  // ===== FROM-JSON

  public static class FromJson extends AbstractFunction {

    public FromJson() {
      super("from-json", 1, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      String json = NodeUtils.toString(arguments[0], true);
      if (json == null)
        return NullNode.instance;

      try {
        JsonNode parsed = NodeUtils.mapper.readTree(json);
        if (parsed == null) // if input is "", for example
          return NullNode.instance;
        return parsed;
      } catch (Exception e) {
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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      try {
        String json = NodeUtils.mapper.writeValueAsString(arguments[0]);
        return new TextNode(json);
      } catch (Exception e) {
        throw new JsltException("to-json can't serialize " + arguments[0] + ": " + e);
      }
    }
  }

  // ===== JOIN

  public static class Join extends AbstractFunction {

    public Join() {
      super("join", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      ArrayNode array = NodeUtils.toArray(arguments[0], true);
      if (array == null)
        return NullNode.instance;

      String sep = NodeUtils.toString(arguments[1], false);

      StringBuilder buf = new StringBuilder();
      for (int ix = 0; ix < array.size(); ix++) {
        if (ix > 0)
          buf.append(sep);
        buf.append(NodeUtils.toString(array.get(ix), false));
      }
      return new TextNode(buf.toString());
    }
  }

  // ===== CONTAINS

  public static class Contains extends AbstractFunction {

    public Contains() {
      super("contains", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      if (arguments[1].isNull())
        return BooleanNode.FALSE; // nothing is contained in null

      else if (arguments[1].isArray()) {
        for (int ix = 0; ix < arguments[1].size(); ix++)
          if (arguments[1].get(ix).equals(arguments[0]))
            return BooleanNode.TRUE;

      } else if (arguments[1].isObject()) {
        String key = NodeUtils.toString(arguments[0], true);
        if (key == null)
          return BooleanNode.FALSE;

        return NodeUtils.toJson(arguments[1].has(key));

      } else if (arguments[1].isTextual()) {
        String sub = NodeUtils.toString(arguments[0], true);
        if (sub == null)
          return BooleanNode.FALSE;

        String str = arguments[1].asText();
        return NodeUtils.toJson(str.indexOf(sub) != -1);

      } else
        throw new JsltException("Contains cannot operate on " + arguments[1]);

      return BooleanNode.FALSE;
    }
  }

  // ===== SIZE

  public static class Size extends AbstractFunction {

    public Size() {
      super("size", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      if (arguments[0].isArray() || arguments[0].isObject())
        return new IntNode(arguments[0].size());

      else if (arguments[0].isTextual())
        return new IntNode(arguments[0].asText().length());

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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      String msg = NodeUtils.toString(arguments[0], false);
      throw new JsltException("error: " + msg);
    }
  }

  // ===== STRING

  public static class ToString extends AbstractFunction {

    public ToString() {
      super("string", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      if (arguments[0].isTextual())
        return arguments[0];
      else
        return new TextNode(arguments[0].toString());
    }
  }

  // ===== IS-STRING

  public static class IsString extends AbstractFunction {

    public IsString() {
      super("is-string", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(arguments[0].isTextual());
    }
  }

  // ===== IS-NUMBER

  public static class IsNumber extends AbstractFunction {

    public IsNumber() {
      super("is-number", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(arguments[0].isNumber());
    }
  }

  // ===== NOW

  public static class Now extends AbstractFunction {

    public Now() {
      super("now", 0, 0);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      long ms = System.currentTimeMillis();
      return NodeUtils.toJson( ms / 1000.0 );
    }
  }

  // ===== PARSE-TIME

  public static class ParseTime extends AbstractFunction {

    public ParseTime() {
      super("parse-time", 2, 3);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      String text = NodeUtils.toString(arguments[0], true);
      if (text == null)
        return NullNode.instance;

      String formatstr = NodeUtils.toString(arguments[1], false);
      JsonNode fallback = null;
      if (arguments.length > 2)
        fallback = arguments[2];

      // the performance of this could be better, but it's not so easy
      // to fix that when SimpleDateFormat isn't thread-safe, so we
      // can't safely share it between threads

      try {
        SimpleDateFormat format = new SimpleDateFormat(formatstr);
        format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        Date time = format.parse(text);
        return NodeUtils.toJson((double) (time.getTime() / 1000.0));
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

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode number = NodeUtils.number(arguments[0], null);
      if (number == null || number.isNull())
        return NullNode.instance;

      double timestamp = number.asDouble();

      String formatstr = NodeUtils.toString(arguments[1], false);

      TimeZone zone = new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC");
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
        return new TextNode(formatted);
      } catch (IllegalArgumentException e) {
        // thrown if format is bad
        throw new JsltException("format-time: Couldn't parse format '" + formatstr + "': " + e.getMessage());
      }
    }
  }
}
