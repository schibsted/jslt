
package com.schibsted.spt.data.jstl2.impl;

import java.util.Iterator;
import java.util.regex.Pattern;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
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
      return min;
    }
  }

  // ===== NUMBER

  public static class Number extends AbstractFunction {

    public Number() {
      super("number", 1, 1);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      return BuiltinFunctions.number(arguments[0]);
    }
  }

  // ===== CONVERTERS

  public static JsonNode toJson(boolean value) {
    if (value)
      return BooleanNode.TRUE;
    else
      return BooleanNode.FALSE;
  }

  public static String toString(JsonNode value) {
    // check what type this is
    if (value.isTextual())
      return value.asText();

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

  // ===== TEST

  public static class Test extends AbstractFunction {

    public Test() {
      super("test", 2, 2);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      // if data is missing then it doesn't match, end of story
      if (arguments[0].isNull())
        return BooleanNode.FALSE;

      String string = BuiltinFunctions.toString(arguments[0]);
      String regexp = BuiltinFunctions.toString(arguments[1]);

      return toJson(Pattern.matches(regexp, string));
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

      byte[] string = BuiltinFunctions.toString(arguments[0]).getBytes(UTF_8);
      byte[] regexp = BuiltinFunctions.toString(arguments[1]).getBytes(UTF_8);

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
}
