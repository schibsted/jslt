
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

public class ParseBenchmark {
  private static final int WARMUP = 5;
  private static final int ITERATIONS = 100;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: java com.schibsted.spt.data.jslt.cli.Benchmark <json input file>");
      System.exit(1);
    }

    System.out.println("Warmup ...");
    for (int ix = 0; ix < WARMUP; ix++) {
      Iterator<JsonValue> it = JsonIO.parseLines(new FileReader(args[0]));
      while (it.hasNext())
        it.next();
    }

    System.out.println("Timing ...");
    long start = System.currentTimeMillis();
    for (int ix = 0; ix < ITERATIONS; ix++) {
      Iterator<JsonValue> it = JsonIO.parseLines(new FileReader(args[0]));
      while (it.hasNext())
        it.next();
    }
    long duration = System.currentTimeMillis() - start;
    System.out.println("Time: " + duration);
  }
}
