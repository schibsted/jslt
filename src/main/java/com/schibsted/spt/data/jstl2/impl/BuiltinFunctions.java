
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.Expression;
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

    public JsonNode call(JsonNode input, Expression[] arguments) {
      // get the argument value
      JsonNode value = arguments[0].apply(input);

      // check what type this is before moving on
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
        throw new JstlException("number(" + value + ") failed: not a number");
      }
    }
  }
}
