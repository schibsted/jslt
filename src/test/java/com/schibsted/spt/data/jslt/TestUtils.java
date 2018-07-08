
package com.schibsted.spt.data.jslt;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {
  private static ObjectMapper mapper = new ObjectMapper();

  public static JsonNode loadJson(String resource) {
    try (InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream(resource)) {
      if (stream == null)
        throw new JsltException("Cannot load resource '" + resource + "': not found");

      Reader reader = new InputStreamReader(stream, "UTF-8");
      return mapper.readTree(reader);
    } catch (IOException e) {
      throw new JsltException("Couldn't read resource " + resource, e);
    }
  }
}
