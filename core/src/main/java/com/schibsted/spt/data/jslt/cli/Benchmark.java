
package com.schibsted.spt.data.jslt.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.schibsted.spt.data.json.*;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.impl.ExpressionImpl;

public class Benchmark {
  private static final int WARMUP = 5;
  private static final int ITERATIONS = 100;

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: java com.schibsted.spt.data.jslt.cli.Benchmark <jslt directory> <json input file>");
      System.exit(1);
    }

    List<Expression> expressions = new ArrayList<>();
    File root = new File(args[0]);
    for (String jsltFile : root.list()) {
      expressions.add(Parser.compile(new File(root, jsltFile)));
    }
    System.out.println("Expressions: " + expressions.size());

    List<JsonValue> values = new ArrayList<>();
    Iterator<JsonValue> it = JsonIO.parseLines(new FileReader(args[1]));
    while (it.hasNext())
      values.add(it.next());
    System.out.println("JSON objects: " + values.size());

    System.out.println("Warmup ...");
    run(expressions, values, WARMUP);

    System.out.println("Timing ...");
    long start = System.currentTimeMillis();
    run(expressions, values, ITERATIONS);
    long duration = System.currentTimeMillis() - start;
    System.out.println("Time: " + duration);
  }

  private static void run(List<Expression> expressions, List<JsonValue> values, int times) {
    for (int ix = 0; ix < times; ix++) {
      for (Expression expr : expressions) {
        for (JsonValue value : values) {
          expr.apply(value);
        }
      }
    }
  }

}
