
package com.schibsted.spt.data.json;

import java.io.Writer;
import java.io.IOException;
import com.schibsted.spt.data.jslt.JsltException;

public class SerializingJsonHandler implements JsonEventHandler {
  private Writer out;
  private boolean[] firstStack;
  private boolean[] arrayStack;
  private int stackPos;

  public SerializingJsonHandler(Writer out) {
    this.out = out;
    this.firstStack = new boolean[20];
    this.arrayStack = new boolean[20];
    this.stackPos = -1;
  }

  public void handleString(String value) {
    try {
      addArrayComma();
      out.write('"' + value + '"');
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleLong(long value) {
    try {
      addArrayComma();
      out.write("" + value);
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleDouble(double value) {
    try {
      addArrayComma();
      out.write("" + value);
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleBoolean(boolean value) {
    try {
      addArrayComma();
      out.write("" + value);
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleNull() {
    try {
      addArrayComma();
      out.write("null");
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void startObject() {
    try {
      addArrayComma();
      out.write("{");
      firstStack[++stackPos] = true;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleKey(String key) {
    try {
      if (firstStack[stackPos])
        firstStack[stackPos] = false;
      else
        out.write(",");
      handleString(key);
      out.write(":");
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void endObject() {
    try {
      out.write("}");
      stackPos--;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void startArray() {
    try {
      addArrayComma();
      out.write("[");
      firstStack[++stackPos] = true;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void endArray() {
    try {
      out.write("]");
      stackPos--;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  private void addArrayComma() throws IOException {
    if (stackPos >= 0) {
      if (arrayStack[stackPos] && firstStack[stackPos])
        out.write(",");
      else
        firstStack[stackPos] = false;
    }
  }
}
