
package com.schibsted.spt.data.json;

import java.io.Reader;
import java.io.IOException;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonParser {
  private Reader source;
  private JsonEventHandler handler;
  private char[] buffer;
  private char[] tmp;
  private int nextIx;
  private static final int BUFFER_SIZE = 65368;

  // reasonable inputs:
  //   string from a line parser
  //   byte[] from kafka
  //   inputstream
  //   reader

  public JsonParser() throws IOException {
    this.buffer = new char[BUFFER_SIZE];
    this.tmp = new char[BUFFER_SIZE]; // buffer used for string parsing
  }

  public JsonValue parse(Reader source) throws IOException {
    JsonBuilderHandler builder = new JsonBuilderHandler();
    parse(source, builder);
    return builder.get();
  }

  public void parse(Reader source, JsonEventHandler handler) throws IOException {
    this.source = source;
    this.handler = handler;
    this.nextIx = 0;
    fillBuffer();

    int pos = parse(0);
    if (pos != nextIx)
      throw new JsltException("Garbage at end: '" + new String(buffer, pos, nextIx - pos));
  }

  private int parse(int pos) throws IOException {
    pos = consumeWhitespace(pos);

    // next character must be one of: { [ " digit t f n
    if (buffer[pos] == '"')
      pos = parseString(pos, false);
    else if (buffer[pos] == '{')
      pos = parseObject(pos);
    else if (buffer[pos] == '[')
      pos = parseArray(pos);
    else if (isDigit(pos) || buffer[pos] == '-')
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

    return consumeWhitespace(pos);
  }

  private int parseString(int pos, boolean isObjectKey) {
    pos++; // we already checked the '"'

    int ix = 0;
    // tmp: buffer allocated together with parser object and reused
    while (buffer[pos] != '"') {
      if (buffer[pos] == '\\') {
        pos++;
        char ch = buffer[pos++];
        if (ch == 't')
          ch = '\t';
        else if (ch == 'n')
          ch = '\n';
        else if (ch == 'r')
          ch = '\r';
        else if (ch != '\\' && ch != '"')
          throw new JsltException("Expected \\ or \"");

        tmp[ix++] = ch;
      } else
        tmp[ix++] = buffer[pos++];
    }

    if (isObjectKey)
      handler.handleKey(new String(tmp, 0, ix));
    else
      handler.handleString(new String(tmp, 0, ix));
    return ++pos;
  }

  private int parseNumber(int pos) {
    // FIXME: we already have code for this ...
    int sign = 1;
    if (buffer[pos] == '-') {
      sign = -1;
      pos++;
    }

    int start = pos++;
    while (pos < nextIx && isDigit(pos))
      pos++;

    long intPart = sign * Long.parseLong(new String(buffer, start, pos - start));
    if (pos == nextIx || buffer[pos] != '.') {
      // FIXME: we *don't* want to allocate a string
      handler.handleLong(intPart);
      return pos;
    }

    pos++; // step over the '.'
    start = pos;
    while (pos < nextIx && isDigit(pos))
      pos++;

    long decimalPart = sign * Long.parseLong(new String(buffer, start, pos - start));
    int digits = pos - start;
    double number = (decimalPart / Math.pow(10, digits)) + intPart;

    if (buffer[pos] == 'E' || buffer[pos] == 'e') {
      pos++;
      int exponentSign = 1;
      if (buffer[pos] == '-') {
        exponentSign = -1;
        pos++;
      } else if (buffer[pos] == '+')
        pos++;

      start = pos;
      while (pos < nextIx && isDigit(pos))
        pos++;
      int exp = Integer.parseInt(new String(buffer, start, pos - start));;
      number = number * Math.pow(10, exp * exponentSign);
    }

    handler.handleDouble(number);
    return pos;
  }

  private int parseObject(int pos) throws IOException {
    pos++; // step over the '{'
    handler.startObject();

    pos = consumeWhitespace(pos);
    boolean first = true;
    while (buffer[pos] != '}') {
      if (first)
        first = false;
      else {
        verifyAt(',', pos++);
        pos = consumeWhitespace(pos);
      }

      verifyAt('"', pos);
      pos = parseString(pos, true);

      pos = consumeWhitespace(pos);
      verifyAt(':', pos);

      pos = parse(pos + 1);
      pos = consumeWhitespace(pos);
    }

    handler.endObject();
    return pos + 1; // step over '}'
  }

  private int parseArray(int pos) throws IOException {
    pos++; // step over the '['
    handler.startArray();

    pos = consumeWhitespace(pos);
    boolean first = true;
    while (buffer[pos] != ']') {
      if (first)
        first = false;
      else {
        verifyAt(',', pos++);
        pos = consumeWhitespace(pos);
      }

      pos = parse(pos);
      pos = consumeWhitespace(pos);
    }

    handler.endArray();
    return pos + 1; // step over ']'
  }

  private void verifyAt(char ch, int pos) {
    if (buffer[pos] != ch)
      throw new JsltException("Expected '\"' at " + pos + ", but got '" +
                              buffer[pos] + "'");
  }

  private boolean isDigit(int pos) {
    char ch = buffer[pos];
    return ch >= '0' && ch <= '9';
  }

  private int verify(int pos, String text) throws IOException {
    for (int ix = 0; ix < text.length(); ix++)
      if (buffer[pos + ix] != text.charAt(ix))
        throw new JsltException("Expected " + text + " found something else");

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
    return buffer[ix] == ' ' || buffer[ix] == '\n' || buffer[ix] == '\r'
      || buffer[ix] == '\t';
  }
}
