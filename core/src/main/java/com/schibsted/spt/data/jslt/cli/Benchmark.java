
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

    List<ExpressionMeta> expressions = new ArrayList<>();
    File root = new File(args[0]);
    for (String jsltFile : root.list())
      expressions.add(new ExpressionMeta(Parser.compile(new File(root, jsltFile)), jsltFile.indexOf("filter") != -1));
    System.out.println("Expressions: " + expressions.size());

    List<byte[]> values = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new FileReader(args[1]));
    String line = reader.readLine();
    while (line != null)
      values.add(line.getBytes());
    System.out.println("JSON objects: " + values.size());

    System.out.println("Warmup ...");
    run(expressions, values, WARMUP);

    System.out.println("Timing ...");
    long start = System.currentTimeMillis();
    run(expressions, values, ITERATIONS);
    long duration = System.currentTimeMillis() - start;
    System.out.println("Time: " + duration);
  }

  private static JsonParser parser = new JsonParser();
  private static JsonWriter writer = new JsonWriter();
  private static void run(List<ExpressionMeta> expressions, List<byte[]> values, int times) throws IOException {
    for (int ix = 0; ix < times; ix++) {
      for (byte[] value : values) {
        JsonValue v = parser.parse(value);
        for (ExpressionMeta expr : expressions) {
          JsonValue v2 = expr.expr.apply(v);
          if (expr.isFilter)
            writer.toBytes(v2);
        }
      }
    }
  }

  static class ExpressionMeta {
    Expression expr;
    boolean isFilter;

    ExpressionMeta(Expression expr, boolean isFilter) {
      this.expr = expr;
      this.isFilter = isFilter;
    }
  }
}
