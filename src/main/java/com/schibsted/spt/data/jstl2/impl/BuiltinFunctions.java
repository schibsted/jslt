
package com.schibsted.spt.data.jstl2.impl;

import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
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
}
