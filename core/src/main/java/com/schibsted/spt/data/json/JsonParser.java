
package com.schibsted.spt.data.json;

import java.io.Reader;
import java.io.IOException;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonParser {
  private Reader source;
  private JsonEventHandler handler;
  private char[] buffer;
  private int nextIx;
  private static final int BUFFER_SIZE = 65368;

  // reasonable inputs:
  //   string from a line parser
  //   byte[] from kafka
  //   inputstream
  //   reader

  // use reader as source interface, read into locally maintained buffer

  public JsonParser(Reader source, JsonEventHandler handler) throws IOException {
    this.source = source;
    this.buffer = new char[BUFFER_SIZE];
    this.handler = handler;
    fillBuffer();
  }

  public void parse() throws IOException {
    int pos = consumeWhitespace(0);

    // next character must be one of: { [ " digit t f n
    if (buffer[pos] == '"')
      pos = parseString(pos);
    else if (isDigit(pos))
      pos = parseNumber(pos);
    else if (buffer[pos] == 't') {
      pos = verify(pos, "true");
      handler.handleBoolean(true);
    } else if (buffer[pos] == 'f') {
      pos = verify(pos, "false");
      handler.handleBoolean(false);
    } else if (buffer[pos] == 'n') {
      pos = verify(pos, "null");
      handler.handleNull();
    } else
      throw new JsltException("ERROR at " + pos);

    if (pos != nextIx)
      throw new JsltException("Garbage at end");
  }

  private int parseString(int pos) {
    pos++; // we already checked the '"'
    int start = pos;
    while (buffer[pos] != '"')
      pos++;
    handler.handleString(new String(buffer, start, pos - start));
    return ++pos;
  }

  private int parseNumber(int pos) {
    // we already have code for this ...
    int start = pos++;
    while (isDigit(pos))
      pos++;

    // FIXME: this is hardly optimal
    handler.handleLong(Long.parseLong(new String(buffer, start, pos - start)));

    return pos;
  }

  private boolean isDigit(int pos) {
    char ch = buffer[pos];
    return ch >= '0' && ch <= '9';
  }

  private int verify(int pos, String text) throws IOException {
    for (int ix = 0; ix < text.length(); ix++)
      if (buffer[pos + ix] != text.charAt(ix))
        throw new JsltException("Expected " + text + " found sometehing else");

    return pos + text.length();
  }

  private void fillBuffer() throws IOException {
    int chars = source.read(buffer, nextIx, buffer.length - nextIx);
    nextIx += chars;
  }

  private int consumeWhitespace(int ix) {
    while (ix < nextIx && isWhitespace(ix))
      ix++;

    //if (ix ==
    return ix;
  }

  private boolean isWhitespace(int ix) {
    return buffer[ix] == ' ';
  }
}
