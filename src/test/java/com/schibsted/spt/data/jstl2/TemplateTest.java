
package com.schibsted.spt.data.jstl2;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Test cases verifying templates.
 */
public class TemplateTest extends TestBase {

  @Test
  public void testTemplate() {
    check("{\"foo\" : 2}", "{\"bar\" : .foo}", "{\"bar\" : 2}");
  }

  @Test
  public void testIfTemplate() {
    String template = "{\"bar\" : if (.foo) .foo else .bar}";
    check("{\"foo\" : 2}", template, "{\"bar\" : 2}");
    check("{\"bar\" : 2}", template, "{\"bar\" : 2}");
    check("{\"baz\" : 2}", template, "{}");
  }

  @Test
  public void testComment() {
    check("{\"foo\" : 2}", "// tuut tuut\n" +
                           "{\"bar\" : .foo}", "{\"bar\" : 2}");
  }

  @Test
  public void testTopLevelLet() {
    check("{\"foo\" : 2}", "let foo = 2 " +
                           "{\"bar\" : $foo}", "{\"bar\" : 2}");
  }

  @Test
  public void testObjectLet() {
    check("{\"foo\" : 2}", "{let foo = 2 " +
                           "\"bar\" : $foo}", "{\"bar\" : 2}");
  }

  @Test
  public void testIfLet() {
    check("{\"foo\" : 2}",
          "{\"bar\" : if (.foo) " +
          "             let var = .foo " +
          "             $var " +
          "           else .bar }",
          "{\"bar\" : 2}");
  }

  @Test
  public void testIfElseLet() {
    check("{\"bar\" : 2}",
          "{\"bar\" : if (.foo) " +
          "             .foo " +
          "           else " +
          "             let var = 234 " +
          "             $var }",
          "{\"bar\" : 234}");
  }

  @Test
  public void testBasicMatching() {
    check("{\"bar\" : 2, \"baz\" : 14}",

          "{* : .}",

          "{\"bar\" : 2, " +
          " \"baz\" : 14 }");
  }

  @Test
  public void testBasicMatching2() {
    check("{\"bar\" : 2, \"baz\" : 14}",

          "{\"bille\" : 100, " +
          " \"foo\" : .bar, " +
          " \"bar\" : 200, " +
          " * : .}",

          "{\"bille\" : 100, " +
          " \"foo\" : 2, " +
          " \"bar\" : 200, " +
          " \"baz\" : 14 }");
  }

  @Test
  public void testMatchArray() {
    error("[1,2,3,4,5]",

          "{\"bille\" : 100, " +
          " \"foo\" : .bar, " +
          " \"bar\" : 200, " +
          " * : .}",

          "match");
  }

  @Test
  public void testMatchArrayOnly() {
    error("[1,2,3,4,5]",

          "{\"bille\" : 100, " +
          " \"bar\" : 200, " +
          " * : .}",

          "match");
  }

  @Test
  public void testMatchingNested() {
    check("{\"bille\" : { " +
          "   \"type\" : 14, " +
          "   \"hey\" : 18 " +
          " } " +
          "}",

          "{\"bille\" : { " +
          "   \"hey\" : 22, " +
          "   * : . " +
          " } " +
          "}",

          "{\"bille\" : { " +
          "   \"type\" : 14, " +
          "   \"hey\" : 22 " +
          " } " +
          "}");
  }

  @Test
  public void testMatchingInLet() {
    check("{ " +
          "  \"if\" : \"View\", " +
          "  \"else\" : false " +
          "}",

          "{ " +
          "  let bar = {* : .} " +

          "  \"fish\" : \"barrel\", " +
          "  \"copy\" : $bar " +
          "}",

          "{" +
          "  \"fish\" : \"barrel\", " +
          "  \"copy\" : { " +
          "    \"if\" : \"View\", " +
          "    \"else\" : false " +
          "  } " +
          "}");
  }

  @Test
  public void testMatchingInNestedLet() {
    check("{ " +
          "  \"if\" : \"View\", " +
          "  \"else\" : false " +
          "}",

          "{ " +
          "  \"fish\" : \"barrel\", " +
          "  \"copy\" : { " +
          "    let bar = {* : .} " +
          "    \"dongle\" : $bar " +
          "  } " +
          "}",

          "{" +
          "  \"fish\" : \"barrel\" " +
          "}");
  }

  @Test
  public void testMatchingInIf() {
    check("{\"bille\" : { " +
          "   \"type\" : 14, " +
          "   \"hey\" : 18 " +
          " } " +
          "}",

          "{ " +
          "  \"fish\" : \"barrel\", " +
          "  \"bille\" : if ( .bille ) {* : .} " +
          "}",

          "{" +
          "  \"fish\" : \"barrel\", " +
          "  \"bille\" : { " +
          "    \"type\" : 14, " +
          "    \"hey\" : 18 " +
          "  } " +
          "}");
  }

  // NEXT: matching-7
}
