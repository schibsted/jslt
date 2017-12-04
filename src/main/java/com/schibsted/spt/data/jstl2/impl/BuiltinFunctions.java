
package com.schibsted.spt.data.jstl2.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.LongNode;

// used for the capture() function
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.NameEntry;

import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;

public class BuiltinFunctions {

  // this will be replaced with a proper Context. need to figure out
  // relationship between compile-time and run-time context first.
  public static Map<String, Function> functions = new HashMap();
  static {
    functions.put("number", new BuiltinFunctions.Number());
    functions.put("round", new BuiltinFunctions.Round());
    functions.put("test", new BuiltinFunctions.Test());
    functions.put("capture", new BuiltinFunctions.Capture());
    functions.put("split", new BuiltinFunctions.Split());
    functions.put("join", new BuiltinFunctions.Join());
    functions.put("lowercase", new BuiltinFunctions.Lowercase());
    functions.put("not", new BuiltinFunctions.Not());
    functions.put("fallback", new BuiltinFunctions.Fallback());
    functions.put("is-object", new BuiltinFunctions.IsObject());
    functions.put("is-array", new BuiltinFunctions.IsArray());
    functions.put("starts-with", new BuiltinFunctions.StartsWith());
    functions.put("ends-with", new BuiltinFunctions.EndsWith());
    functions.put("contains", new BuiltinFunctions.Contains());
  }

  private static abstract class AbstractFunction implements Function {
    private String name;
    private int min;
    private int max;

    public AbstractFunction(String name, int min, int max) {
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

  // ===== NUMBER

  public static class Number extends AbstractFunction {

    public Number() {
      super("number", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.number(arguments[0]);
    }
  }

  // ===== TEST

  public static class Test extends AbstractFunction {

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
        throw new JstlException("test() can't test null regexp");

      Pattern p = Pattern.compile(regexp);
      java.util.regex.Matcher m = p.matcher(string);
      return NodeUtils.toJson(m.find(0));
    }
  }

  // ===== CAPTURE

  // believe it or not, but the built-in Java regex library is so
  // incredibly shitty that it doesn't allow you to learn what the
  // names of the named groups are. so we are using this library,
  // which does solve that. (lots of swearing omitted.)

  public static class Capture extends AbstractFunction {

    public Capture() {
      super("capture", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      // if data is missing then it doesn't match, end of story
      if (arguments[0].isNull())
        return arguments[0]; // null

      byte[] string = NodeUtils.toString(arguments[0], false).getBytes(UTF_8);
      String regexps = NodeUtils.toString(arguments[1], true);
      if (regexps == null)
        throw new JstlException("capture() can't match against null regexp");
      byte[] regexp = regexps.getBytes(UTF_8);

      Regex regex = new Regex(regexp, 0, regexp.length, Option.NONE, UTF8Encoding.INSTANCE);
      Matcher matcher = regex.matcher(string);
      int result = matcher.search(0, string.length, Option.DEFAULT);

      ObjectNode node = NodeUtils.mapper.createObjectNode();

      if (result != -1) {
        Region region = matcher.getEagerRegion();
        for (Iterator<NameEntry> entry = regex.namedBackrefIterator(); entry.hasNext();) {
          NameEntry e = entry.next();
          int number = e.getBackRefs()[0]; // can have many refs per name
          int begin = region.beg[number];
          int end = region.end[number];

          byte[] name = e.name;
          node.put(new String(name, e.nameP, e.nameEnd - e.nameP, UTF_8),
                   new String(string, begin, end - begin, UTF_8));
        }
      }

      return node;
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
        throw new JstlException("split() can't split on null");

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

  // ===== NOT

  public static class Not extends AbstractFunction {

    public Not() {
      super("not", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(!NodeUtils.isTrue(arguments[0]));
    }
  }

  // ===== FALLBACK

  public static class Fallback extends AbstractFunction {

    public Fallback() {
      super("fallback", 2, 1024);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      for (int ix = 0; ix < arguments.length; ix++)
        if (NodeUtils.isValue(arguments[ix]))
          return arguments[ix];
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

  // ===== IS-ARRAY

  public static class IsArray extends AbstractFunction {

    public IsArray() {
      super("is-array", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return NodeUtils.toJson(arguments[0].isArray());
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

  // ===== ROUND

  public static class Round extends AbstractFunction {

    public Round() {
      super("round", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode number = NodeUtils.number(arguments[0]);
      if (number.isNull())
        return NullNode.instance;

      return new LongNode(Math.round(number.doubleValue()));
    }
  }

  // ===== CONTAINS

  public static class Contains extends AbstractFunction {

    public Contains() {
      super("contains", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      if (!arguments[1].isArray())
        throw new JstlException("contains cannot work on " + arguments[1]);

      for (int ix = 0; ix < arguments[1].size(); ix++)
        if (arguments[1].get(ix).equals(arguments[0]))
          return BooleanNode.TRUE;
      return BooleanNode.FALSE;
    }
  }
}
