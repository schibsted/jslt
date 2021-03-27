
package com.schibsted.spt.data.jslt.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.json.*;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.impl.ExpressionImpl;

public class JacksonParseBenchmark {
  private static final int WARMUP = 5;
  private static final int ITERATIONS = 100;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: java com.schibsted.spt.data.jslt.cli.Benchmark <json input file>");
      System.exit(1);
    }

    System.out.println("Warmup ...");
    for (int ix = 0; ix < WARMUP; ix++) {
      parseFile(args[0]);
    }

    System.out.println("Timing ...");
    long start = System.currentTimeMillis();
    for (int ix = 0; ix < ITERATIONS; ix++) {
      parseFile(args[0]);
    }
    long duration = System.currentTimeMillis() - start;
    System.out.println("Time: " + duration);
  }

  private static void parseFile(String filename) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line = reader.readLine();
    while (line != null) {
      mapper.readTree(line);
      line = reader.readLine();
    }
  }
}
