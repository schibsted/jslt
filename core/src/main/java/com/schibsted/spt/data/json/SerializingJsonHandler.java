
package com.schibsted.spt.data.json;

import java.io.Writer;
import java.io.IOException;
import java.math.BigInteger;
import com.schibsted.spt.data.jslt.JsltException;

public final class SerializingJsonHandler implements JsonEventHandler {
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
      out.write('"');
      out.write(value);
      out.write('"');
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleString(char[] buffer, int start, int len) {
    try {
      addArrayComma();
      out.write('"');
      out.write(buffer, start, len);
      out.write('"');
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

  public void handleBigInteger(BigInteger value) {
    try {
      addArrayComma();
      out.write(value.toString());
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

  private static char[] falseBuf = "false".toCharArray();
  private static char[] trueBuf = "true".toCharArray();
  public void handleBoolean(boolean value) {
    try {
      addArrayComma();
      if (value)
        out.write(trueBuf, 0, 4);
      else
        out.write(falseBuf, 0, 5);
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  private static char[] nullBuf = "null".toCharArray();
  public void handleNull() {
    try {
      addArrayComma();
      out.write(nullBuf, 0, 4);
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void startObject() {
    try {
      addArrayComma();
      out.write('{');
      if (stackPos + 1 == firstStack.length)
        growStack();
      firstStack[++stackPos] = true;
      arrayStack[stackPos] = false;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void handleKey(String key) {
    try {
      if (firstStack[stackPos])
        firstStack[stackPos] = false;
      else
        out.write(',');
      handleString(key);
      out.write(':');
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void endObject() {
    try {
      out.write('}');
      stackPos--;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void startArray() {
    try {
      addArrayComma();
      out.write('[');
      if (stackPos + 1 == firstStack.length)
        growStack();
      firstStack[++stackPos] = true;
      arrayStack[stackPos] = true;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  public void endArray() {
    try {
      out.write(']');
      stackPos--;
    } catch (IOException e) {
      throw new JsltException("output error", e);
    }
  }

  private void addArrayComma() throws IOException {
    if (stackPos >= 0) {
      if (arrayStack[stackPos] && !firstStack[stackPos])
        out.write(',');
      else
        firstStack[stackPos] = false;
    }
  }

  private void growStack() {
    boolean[] tmp = new boolean[firstStack.length * 2];
    System.arraycopy(firstStack, 0, tmp, 0, firstStack.length);
    firstStack = tmp;

    tmp = new boolean[arrayStack.length * 2];
    System.arraycopy(arrayStack, 0, tmp, 0, arrayStack.length);
    arrayStack = tmp;
  }
}
