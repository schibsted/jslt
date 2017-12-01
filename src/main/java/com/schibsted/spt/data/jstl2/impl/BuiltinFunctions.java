
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
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
      // check what type this is
      if (arguments[0].isNumber())
        return arguments[0];
      else if (!arguments[0].isTextual())
        return NullNode.instance;

      // let's look at this number
      String number = arguments[0].asText();
      try {
        if (number.indexOf('.') != -1)
          return new DoubleNode(Double.parseDouble(number));
        else
          return new IntNode(Integer.parseInt(number));
      } catch (NumberFormatException e) {
        throw new JstlException("number(" + number + ") failed: not a number");
      }
    }
  }
}
