
package com.schibsted.spt.data.jstl2.impl;

import java.util.Iterator;
import java.util.regex.Pattern;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

      return NodeUtils.toJson(Pattern.matches(regexp, string));
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
}
