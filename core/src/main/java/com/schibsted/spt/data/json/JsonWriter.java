
package com.schibsted.spt.data.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonWriter {
  private UTF8SerializingHandler handler;

  public JsonWriter() {
    this.handler = new UTF8SerializingHandler();
  }

  public byte[] toBytes(JsonValue value) {
    handler.reset();
    value.traverse(handler);
    return handler.toByteArray();
  }
}
