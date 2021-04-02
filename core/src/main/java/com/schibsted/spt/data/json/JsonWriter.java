
package com.schibsted.spt.data.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonWriter {
  private ByteArrayOutputStream baos;
  private OutputStreamWriter osw;
  private SerializingJsonHandler handler;

  public JsonWriter() {
    this.baos = new ByteArrayOutputStream();
    this.osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
    this.handler = new SerializingJsonHandler(osw);
  }

  public byte[] toBytes(JsonValue value) {
    try {
      value.traverse(handler);
      osw.flush();
      byte[] buf = baos.toByteArray();
      baos.reset();
      return buf;
    } catch (IOException e) {
      throw new JsltException("impossible error", e);
    }
  }
}
