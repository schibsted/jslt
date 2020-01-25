package com.schibsted.spt.data.jslt;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class BenchmarkHashTest extends TestBase {

    private static String input;

    @BeforeClass
    public static void setUp() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-document-1.json");
        input = new Scanner(is, "utf-8").useDelimiter("\\Z").next(); //convert InputStream to String
    }


    @Test
    @Ignore
    public void benchmarkSha256Hex() {
        Instant start = Instant.now();
        int iterations = 2000000;
        for (int i = 0; i < iterations; i++) {
            execute(input, "sha256-hex(.)");
        }
        Instant end = Instant.now();
        final Duration elapsed = Duration.between(start, end);
        System.out.println("sha256-hex " + iterations + " iterations took " + elapsed.getSeconds() +" seconds. " + ((float)iterations)/elapsed.getSeconds() + " iterations/second");
    }

    @Test
    @Ignore
    public void benchmarkHashint() {
        Instant start = Instant.now();
        int iterations = 2000000;
        for (int i = 0; i < iterations; i++) {
            execute(input, "hash-int(.)");
        }
        Instant end = Instant.now();
        final Duration elapsed = Duration.between(start, end);
        System.out.println("hash-int " + iterations + " iterations took " + elapsed.getSeconds() +" seconds. " + ((float)iterations)/elapsed.getSeconds() + " iterations/second");
    }
}