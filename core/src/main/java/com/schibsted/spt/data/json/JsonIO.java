
package com.schibsted.spt.data.json;

import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonIO {
  private static ObjectMapper mapper = new ObjectMapper();

  public static String toString(JsonValue value) {
    return toStringWithSortedKeys(value);
  }

  public static String toStringWithSortedKeys(JsonValue value) {
    // FIXME: no actual sorting of keys
    StringWriter buf = new StringWriter();
    JsonTraversal.traverse(value, new SerializingJsonHandler(buf));
    return buf.toString();
  }

  public static String prettyPrint(JsonValue value) {
    return toString(value);
  }

  public static JsonValue parseString(String json) {
    // try {
    //   JsonNode node = mapper.readTree(json);
    //   if (node == null)
    //     node = NullNode.instance;
    //   return new JacksonJsonValue(node);
    // } catch (JsonProcessingException e) {
    //   throw new JsltException("parsing failed", e);
    // }
    try {
      JsonBuilderHandler builder = new JsonBuilderHandler();
      JsonParser parser = new JsonParser(new StringReader(json), builder);
      parser.parse();
      return builder.get();
    } catch (IOException e) {
      throw new JsltException("impossible", e);
    }
  }

  public static JsonValue parse(Reader in) throws IOException {
    return new JacksonJsonValue(mapper.readTree(in));
  }

  public static JsonValue parse(File file) throws IOException {
    try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
      return parse(in);
    }
  }
}
