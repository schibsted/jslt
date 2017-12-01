
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

}
