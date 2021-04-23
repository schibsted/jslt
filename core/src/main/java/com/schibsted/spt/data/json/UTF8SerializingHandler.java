
package com.schibsted.spt.data.json;

import java.io.Writer;
import java.math.BigInteger;
import com.schibsted.spt.data.jslt.JsltException;

public final class UTF8SerializingHandler implements JsonEventHandler {
  private int pos;
  private byte[] buffer;

  private boolean[] firstStack;
  private boolean[] arrayStack;
  private int stackPos;

  public UTF8SerializingHandler() {
    this.firstStack = new boolean[20];
    this.arrayStack = new boolean[20];
    this.stackPos = -1;
    this.buffer = new byte[16384];
  }

  public void reset() {
    this.pos = 0;
  }

  public void handleString(String value) {
    addArrayComma();
    writeAscii('"');
    writeString(value);
    writeAscii('"');
  }

  public void handleString(char[] buffer, int start, int len) {
    addArrayComma();
    writeAscii('"');
    writeString(buffer, start, len);
    writeAscii('"');
  }

  public void handleLong(long value) {
    addArrayComma();
    writeAscii("" + value);
  }

  public void handleBigInteger(BigInteger value) {
    addArrayComma();
    writeAscii(value.toString());
  }

  public void handleDouble(double value) {
    addArrayComma();
    writeAscii("" + value);
  }

  private static char[] falseBuf = "false".toCharArray();
  private static char[] trueBuf = "true".toCharArray();
  public void handleBoolean(boolean value) {
    addArrayComma();
    if (value)
      writeAscii(trueBuf, 0, 4);
    else
      writeAscii(falseBuf, 0, 5);
  }

  private static char[] nullBuf = "null".toCharArray();
  public void handleNull() {
    addArrayComma();
    writeAscii(nullBuf, 0, 4);
  }

  public void startObject() {
    addArrayComma();
    writeAscii('{');
    firstStack[++stackPos] = true;
    arrayStack[stackPos] = false;
  }

  public void handleKey(String key) {
    if (firstStack[stackPos])
      firstStack[stackPos] = false;
    else
      writeAscii(',');
    handleString(key);
    writeAscii(':');
  }

  public void endObject() {
    writeAscii('}');
    stackPos--;
  }

  public void startArray() {
    addArrayComma();
    writeAscii('[');
    firstStack[++stackPos] = true;
    arrayStack[stackPos] = true;
  }

  public void endArray() {
    writeAscii(']');
    stackPos--;
  }

  private void addArrayComma() {
    if (stackPos >= 0) {
      if (arrayStack[stackPos] && !firstStack[stackPos])
        writeAscii(',');
      else
        firstStack[stackPos] = false;
    }
  }

  public byte[] toByteArray() {
    byte[] out = new byte[pos];
    System.arraycopy(buffer, 0, out, 0, pos);
    return out;
  }

  // ===== INTERNAL I/O METHODS

  private void writeAscii(char ch) {
    buffer[pos++] = (byte) ch;
  }

  private void writeAscii(String str) {
    for (int ix = 0; ix < str.length(); ix++)
      buffer[pos++] = (byte) str.charAt(ix);
  }

  private void writeAscii(char[] tmp, int offset, int len) {
    for (int ix = offset; ix < offset + len; ix++)
      buffer[pos++] = (byte) tmp[ix];
  }

  private void writeString(String str) {
    for (int ix = 0; ix < str.length(); ix++) {
      char ch = str.charAt(ix);
      if (ch == '"' || ch == '\\') {
        buffer[pos++] = (byte) '\\';
        buffer[pos++] = (byte) ch;
      } else if (ch <= 0x1F)
        escapeControl(ch);
      else if (ch <= 0x7F)
        buffer[pos++] = (byte) ch;
      else
        writeEncoded(ch);
    }
  }

  private void writeString(char[] tmp, int offset, int len) {
    for (int ix = offset; ix < offset + len; ix++) {
      if (tmp[ix] <= 0x7F)
        buffer[pos++] = (byte) tmp[ix];
      else
        writeEncoded(tmp[ix]);
    }
  }

  private void writeEncoded(char ch) {
    if (ch < 0x07FF) {
      // 110xxxxx 10xxxxxx
      buffer[pos++] = (byte) ((ch >> 6) | 0xC0);
      buffer[pos++] = (byte) ((ch & 0x003F) | 0x80);
    } else {
      //1110xxxx 10xxxxxx 10xxxxxx
      buffer[pos++] = (byte) ((ch >> 12) | 0xE0);
      buffer[pos++] = (byte) (((ch >> 6) & 0x00CF) | 0x80);
      buffer[pos++] = (byte) ((ch & 0x00CF) | 0x80);
    }
  }

  // 0x00 - 0x1F
  private void escapeControl(char ch) {
    buffer[pos++] = (byte) '\\';
    if (ch == 0x0A)
      buffer[pos++] = (byte) 'n';
    else if (ch == 0x0D)
      buffer[pos++] = (byte) 'r';
    else if (ch == 0x08)
      buffer[pos++] = (byte) 'b';
    else if (ch == 0x09)
      buffer[pos++] = (byte) 't';
    else if (ch == 0x0C)
      buffer[pos++] = (byte) 'f';
    else {
      buffer[pos++] = (byte) 'u';
      buffer[pos++] = (byte) '0';
      buffer[pos++] = (byte) '0';
      buffer[pos++] = (byte) hexDigit(ch >> 4);
      buffer[pos++] = (byte) hexDigit(ch & 0x000F);
    }
  }

  private char hexDigit(int ch) {
    if (ch < 10)
      return (char) (ch + '0');
    else
      return (char) ((ch - 10) + 'A');
  }
}
