
package com.schibsted.spt.data.jslt;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class TestUtils {
  private static ObjectMapper jsonMapper = new ObjectMapper();
  private static ObjectMapper yamlMapper = new ObjectMapper(
    new YAMLFactory()
  );

  public static JsonNode loadFile(String resource) {
    try (InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream(resource)) {
      if (stream == null)
        throw new JsltException("Cannot load resource '" + resource + "': not found");

      Reader reader = new InputStreamReader(stream, "UTF-8");
      if (resource.endsWith(".json"))
        return jsonMapper.readTree(reader);
      else if (resource.endsWith(".yaml"))
        return yamlMapper.readTree(reader);
      else
        throw new JsltException("Unknown format: " + resource);
    } catch (IOException e) {
      throw new JsltException("Couldn't read resource " + resource, e);
    }
  }
}
