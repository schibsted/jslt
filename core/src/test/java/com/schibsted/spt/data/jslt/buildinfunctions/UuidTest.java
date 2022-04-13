package com.schibsted.spt.data.jslt.buildinfunctions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.TestBase;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class UuidTest extends TestBase {
  private static ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testUuidWithoutParameterMatchesRegex() throws JsonProcessingException {
    Expression given = Parser.compileString("uuid()");
    String actual = mapper.writeValueAsString(given.apply(null));

    String uuidRegex = "^\"[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\"$";
    assertTrue(actual.matches(uuidRegex));
  }

  @Test
  public void testUuidWithoutParameterGeneratesRandomValues() throws JsonProcessingException {
    Expression given = Parser.compileString("{ \"uuid1\" : uuid(), \"uuid2\" : uuid() }");
    JsonNode result = given.apply(null);
    String actual1 = mapper.writeValueAsString(result.findValue("uuid1"));
    String actual2 = mapper.writeValueAsString(result.findValue("uuid2"));

    assertNotEquals(actual1, actual2);
  }

  @Test(expected = JsltException.class)
  public void testUuidWithOneParameterRaisesJsltException() {
    Expression given = Parser.compileString("uuid(123)");
    given.apply(null);
  }
}