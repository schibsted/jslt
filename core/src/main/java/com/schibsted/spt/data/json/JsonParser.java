
package com.schibsted.spt.data.json;

import java.io.Reader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetDecoder;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonParser {
  private CharacterSource source;
  private JsonEventHandler handler;
  private char[] buffer;
  private char[] tmp;
  private static final int BUFFER_SIZE = 65368;

  private CharsetDecoder decoder;
  private CharBuffer bufferObj;

  // reasonable inputs:
  //   string from a line parser
  //   byte[] from kafka
  //   inputstream
  //   reader

  public JsonParser() {
    this(BUFFER_SIZE);
  }

  public JsonParser(int bufferSize) {
    this.buffer = new char[bufferSize];
    this.tmp = new char[bufferSize]; // buffer used for string parsing
  }

  // not thread-safe, not re-entrant
  public JsonValue parse(Reader source) throws IOException {
    JsonBuilderHandler builder = new JsonBuilderHandler();
    parse(source, builder);
    return builder.get();
  }

  // not thread-safe, not re-entrant
  public void parse(Reader source, JsonEventHandler handler) throws IOException {
    this.source = new ReaderCharacterSource(source, buffer);
    startParsing(handler);
  }

  public JsonValue parse(String source) throws IOException {
    JsonBuilderHandler builder = new JsonBuilderHandler();
    parse(source, builder);
    return builder.get();
  }

  public void parse(String source, JsonEventHandler handler) throws IOException {
    this.source = new CharCharacterSource(source, buffer);
    startParsing(handler);
  }

  public JsonValue parse(char[] data, int start, int end) throws IOException {
    JsonBuilderHandler builder = new JsonBuilderHandler();
    this.source = new CharCharacterSource(data, end);
    startParsing(handler);
    return builder.get();
  }

  public JsonValue parse(byte[] data) throws IOException {
    if (decoder == null) {
      decoder = StandardCharsets.UTF_8.newDecoder();
      bufferObj = CharBuffer.wrap(buffer);
    }
    ByteBuffer dataObj = ByteBuffer.wrap(data);
    CoderResult result = decoder.decode(dataObj, bufferObj, true);

    JsonBuilderHandler builder = new JsonBuilderHandler();
    this.source = new CharCharacterSource(buffer, bufferObj.position());
    bufferObj.rewind();
    startParsing(builder);
    return builder.get();
  }

  private void startParsing(JsonEventHandler handler) throws IOException {
    this.handler = handler;
    this.source.confirmFinished(parse(0));
  }

  private int parse(int pos) throws IOException {
    pos = source.consumeWhitespace(pos);

    // next character must be one of: { [ " digit t f n
    char ch = source.charAt(pos);
    if (ch == '"')
      pos = parseString(pos, false);
    else if (ch == '{')
      pos = parseObject(pos);
    else if (ch == '[')
      pos = parseArray(pos);
    else if (isDigit(ch) || ch == '-')
      pos = parseNumber(pos);
    else if (ch == 't') {
      pos = source.verify(pos, "true");
      handler.handleBoolean(true);
    } else if (ch == 'f') {
      pos = source.verify(pos, "false");
      handler.handleBoolean(false);
    } else if (ch == 'n') {
      pos = source.verify(pos, "null");
      handler.handleNull();
    } else
      throw new JsltException("ERROR at " + pos);

    return source.consumeWhitespace(pos);
  }

  private int parseString(int pos, boolean isObjectKey) {
    pos++; // we already checked the '"'

    int ix = 0;
    // tmp: buffer allocated together with parser object and reused
    while (source.charAt(pos) != '"') {
      if (source.charAt(pos) == '\\') {
        pos++;
        char ch = source.charAt(pos++);
        if (ch == 't')
          ch = '\t';
        else if (ch == 'n')
          ch = '\n';
        else if (ch == 'r')
          ch = '\r';
        else if (ch == 'b')
          ch = '\b';
        else if (ch == 'f')
          ch = '\f';
        else if (ch == 'u') {
          ch = parseUnicodeEscape(pos);
          pos += 4;
        } else if (ch != '\\' && ch != '"' && ch != '/')
          throw new JsltException("Expected \\ or \" or /");

        tmp[ix++] = ch;
      } else
        tmp[ix++] = source.charAt(pos++);
    }

    if (isObjectKey)
      handler.handleKey(new String(tmp, 0, ix));
    else
      handler.handleString(tmp, 0, ix);
    return ++pos;
  }

  private char parseUnicodeEscape(int pos) {
    // INV: pos now points to first hex digit
    int number = 0;
    for (int ix = 0; ix < 4; ix++) {
      char ch = source.charAt(pos + ix);
      if (ch >= '0' && ch <= '9')
        number = (number * 16) + (ch - '0');
      else if (ch >= 'A' && ch <= 'F')
        number = (number * 16) + (ch - 'A') + 10;
      else if (ch >= 'a' && ch <= 'f')
        number = (number * 16) + (ch - 'a') + 10;
      else
        throw new JsltException("Bad unicode escape: '" + ch + "' at " + pos);
    }
    return (char) number;
  }

  // FIXME: this will accept things it shouldn't!!!!
  private int parseNumber(int pos) {
    // FIXME: we already have code for this ...
    int sign = 1;
    if (source.charAt(pos) == '-') {
      sign = -1;
      pos++;
    }

    int start = pos;
    long intPart = 0;
    while (!source.atEnd(pos)) {
      long digitValue = digitValue(source.charAt(pos));
      if (digitValue == -1)
        break;

      if (pos - start == 19) {
        break;
      }

      intPart = intPart * 10 + digitValue;
      pos++;
    }
    intPart = intPart * sign;

    // this is a bigint
    if (pos - start == 19) {
      while (!source.atEnd(pos) && isDigit(source.charAt(pos)))
        pos++;

      handler.handleBigInteger(new BigInteger(source.mkString(start, pos)));
      return pos;
    }

    double number = 0;
    boolean decimal = false;
    if (!source.atEnd(pos) && source.charAt(pos) == '.') {
      pos++; // step over the '.'
      start = pos;
      while (!source.atEnd(pos) && isDigit(source.charAt(pos)))
        pos++;

      long decimalPart = sign * Long.parseLong(source.mkString(start, pos));
      int digits = pos - start;
      number = (decimalPart / Math.pow(10, digits)) + intPart;
      decimal = true;
    } else {
      number = intPart;
    }

    if (!source.atEnd(pos) && (source.charAt(pos) == 'E' || source.charAt(pos) == 'e')) {
      pos++;
      int exponentSign = 1;
      if (source.charAt(pos) == '-') {
        exponentSign = -1;
        pos++;
      } else if (source.charAt(pos) == '+')
        pos++;

      start = pos;
      while (!source.atEnd(pos) && isDigit(source.charAt(pos)))
        pos++;
      int exp = Integer.parseInt(source.mkString(start, pos));
      number = number * Math.pow(10, exp * exponentSign);
      handler.handleDouble(number);
    } else {
      if (decimal)
        handler.handleDouble(number);
      else
        handler.handleLong(intPart);
    }
    return pos;
  }

  private int parseObject(int pos) throws IOException {
    pos++; // step over the '{'
    handler.startObject();

    pos = source.consumeWhitespace(pos);
    boolean first = true;
    while (source.charAt(pos) != '}') {
      if (first)
        first = false;
      else {
        source.verifyAt(',', pos++);
        pos = source.consumeWhitespace(pos);
      }

      source.verifyAt('"', pos);
      pos = parseString(pos, true);

      pos = source.consumeWhitespace(pos);
      source.verifyAt(':', pos);

      pos = parse(pos + 1);
      pos = source.consumeWhitespace(pos);
    }

    handler.endObject();
    return pos + 1; // step over '}'
  }

  private int parseArray(int pos) throws IOException {
    pos++; // step over the '['
    handler.startArray();

    pos = source.consumeWhitespace(pos);
    boolean first = true;
    while (source.charAt(pos) != ']') {
      if (first)
        first = false;
      else {
        source.verifyAt(',', pos++);
        pos = source.consumeWhitespace(pos);
      }

      pos = parse(pos);
      pos = source.consumeWhitespace(pos);
    }

    handler.endArray();
    return pos + 1; // step over ']'
  }

  private boolean isDigit(char ch) {
    return ch >= '0' && ch <= '9';
  }

  private long digitValue(char ch) {
    long v = ch - '0';
    if (v >= 0 && v <= 9)
      return v;
    else
      return -1;
  }

  private static boolean isWhitespace(char ch) {
    return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
  }

  interface CharacterSource {

    public boolean atEnd(int pos);

    public char charAt(int pos);

    public String mkString(int start, int end);

    public void verifyAt(char ch, int pos);

    public int consumeWhitespace(int ix);

    public void confirmFinished(int pos);

    public int verify(int pos, String text) throws IOException;
  }

  private static class ReaderCharacterSource implements CharacterSource {
    private Reader input;
    private char[] buffer;
    private int nextIx;

    private ReaderCharacterSource(Reader input, char[] buffer) throws IOException {
      this.input = input;
      this.buffer = buffer;
      fillBuffer();
    }

    private void fillBuffer() throws IOException {
      int chars = input.read(buffer, nextIx, buffer.length - nextIx);
      nextIx += chars;
    }

    public boolean atEnd(int pos) {
      return pos >= nextIx;
    }

    public char charAt(int pos) {
      return buffer[pos];
    }

    public String mkString(int start, int end) {
      return new String(buffer, start, end - start);
    }

    public void verifyAt(char ch, int pos) {
      if (buffer[pos] != ch)
        throw new JsltException("Expected '" + ch + "' at " + pos + ", but got '" +
                                buffer[pos] + "'");
    }

    public int consumeWhitespace(int ix) {
      while (ix < nextIx && isWhitespace(buffer[ix]))
        ix++;

      return ix;
    }

    public void confirmFinished(int pos) {
      if (pos != nextIx)
        throw new JsltException("Garbage at end: '" + mkString(pos, nextIx));
    }

    public int verify(int pos, String text) throws IOException {
      for (int ix = 0; ix < text.length(); ix++)
        if (buffer[pos + ix] != text.charAt(ix))
          throw new JsltException("Expected " + text + " found something else");

      return pos + text.length();
    }
  }

  private static class CharCharacterSource implements CharacterSource {
    private char[] buffer;
    private int end;

    private CharCharacterSource(String input, char[] buffer) {
      this.buffer = buffer;
      this.end = input.length();
      input.getChars(0, input.length(), buffer, 0);
    }

    private CharCharacterSource(char[] input, int end) {
      this.buffer = input;
      this.end = end;
    }

    public boolean atEnd(int pos) {
      return pos >= end;
    }

    public char charAt(int pos) {
      return buffer[pos];
    }

    public String mkString(int start, int end) {
      return new String(buffer, start, end - start);
    }

    public void verifyAt(char ch, int pos) {
      if (buffer[pos] != ch)
        throw new JsltException("Expected '" + ch + "' at " + pos + ", but got '" +
                                buffer[pos] + "'");
    }

    public int consumeWhitespace(int ix) {
      while (ix < end && isWhitespace(buffer[ix]))
        ix++;

      return ix;
    }

    public void confirmFinished(int pos) {
      if (pos != end)
        throw new JsltException("Garbage at end: '" + mkString(pos, end));
    }

    public int verify(int pos, String text) throws IOException {
      for (int ix = 0; ix < text.length(); ix++)
        if (buffer[pos + ix] != text.charAt(ix))
          throw new JsltException("Expected " + text + " found something else");

      return pos + text.length();
    }
  }
}
