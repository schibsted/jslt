
package com.schibsted.spt.data.jslt;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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
  public void testTemplateNull() {
    check("{\"foo\" : 2}", "{\"bar\" : .foo, \"baz\" : .bar}", "{\"bar\" : 2}");
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
    check("[1,2,3,4,5]",

          "{\"bille\" : 100, " +
          " \"foo\" : .bar, " +
          " \"bar\" : 200, " +
          " * : .}",

          "{\"bille\" : 100, " +
          " \"bar\" : 200}");
  }

  @Test
  public void testMatchArrayOnly() {
    check("[1,2,3,4,5]",

          "{\"bille\" : 100, " +
          " \"bar\" : 200, " +
          " * : .}",

          "{\"bille\" : 100, " +
          " \"bar\" : 200}");
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

  @Test
  public void testMatchingRemove() {
    check("{ " +
          "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\", " +
          "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\", " +
          "  \"published\" : \"2017-05-04T09:13:29+02:00\", " +
          "  \"type\" : \"View\", " +
          "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\", " +
          "  \"url\" : \"http://www.aftenposten.no/\" " +
          "}",

          "{ " +
          "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\", " +
          "  \"taip\" : \"View\", " +
          "  * - type : . " +
          "}",

          "{ " +
          "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\", " +
          "  \"taip\" : \"View\", " +
          "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\", " +
          "  \"published\" : \"2017-05-04T09:13:29+02:00\", " +
          "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\", " +
          "  \"url\" : \"http://www.aftenposten.no/\" " +
          "}");
  }

  @Test
  public void testMatchingInFor() {
    check("{ " +
          "   \"list\" : [ " +
          "     {\"bar\": 1}, " +
          "     {\"bar\": 2} " +
          "   ] " +
          "}",

          "{ " +
          "   \"foo\" : [for ( .list ) " +
          "     {\"loop\" : \"for\", " +
          "     * : . }] " +
          "}",

          "{ " +
          "   \"foo\" : [{ " +
          "     \"loop\" : \"for\", " +
          "     \"bar\" : 1 " +
          "   }, { " +
          "     \"loop\" : \"for\", " +
          "     \"bar\" : 2 " +
          "   } " +
          "]}");
  }

  @Test
  public void testMatchingNoSuchObject() {
    check("null",

          "{ " +
          "  \"foo\" : 5, " +
          "  * : . " +
          "}",

          "{ " +
          "   \"foo\" : 5 " +
          "}");
  }

  @Test
  public void testMatchingNotAnAobject() {
    check("{ " +
          "   \"foo\" : 5, " +
          "   \"bar\" : 2 " +
          "}",

          "{ " +
          "  \"foo\" : { " +
          "    \"bar\" : .bar, " +
          "    * : . " +
          "  } " +
          "}",

          "{\"foo\" : {\"bar\" : 2}}");
  }

  @Test
  public void testMatchingInForInsideAnArray() {
    check("[{ " +
          "   \"foo\" : 5, " +
          "   \"bar\" : 2 " +
          "}]",

          // convoluted template to say: take the top-level array,
          // transform the objects it contains into the same objects,
          // and wrap the whole thing in an array
          "[[for (.) {* : .}]]",

          "[[{\"foo\" : 5, \"bar\" : 2}]]");
  }

  @Test
  public void testMatchingInAnArray() {
    error("[{* : .}]",
          "array");
  }

  @Test
  public void testHandleTrickyTransform() {
    check("{}",
          "{" +
          "  \"provider\": {" +
          "    let urn = .provider.\"@id\""+
          "  }," +
          "  * : ." +
          "}",
          "{}");
  }

  // "matching-8.jstl" should "fail" in {
  //   fail("matching-8.jstl", "empty.json")
  // }

  // "matching-10.jstl" should "remove 'type' and 'id'" in {
  //   verify("matching-10.jstl", "simple.json", "matching-10.json")
  // }

  // "matching-remove.jstl" should "remove 'type'" in {
  //   verify("matching-remove.jstl", "simple.json", "matching-remove.json")
  // }

  // "matching-nested.jstl" should "remove 'baz'" in {
  //   verify("matching-nested.jstl", "matching-nested.json", "matching-nested-out.json")
  // }

  // "matching-bad-1.jstl" should "throw error on parse" in {
  //   parseError("matching-bad-1.jstl")
  // }
}
