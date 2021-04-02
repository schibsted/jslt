
package com.schibsted.spt.data.json;

import java.util.Map;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jslt.JsltException;

public class JacksonJsonValue extends AbstractJsonValue {
  private JsonNode node;

  public JacksonJsonValue(JsonNode node) {
    if (node == null)
      throw new NullPointerException();
    this.node = node;
  }

  public boolean isSequence() {
    return isString() || isArray();
  }

  public boolean isString() {
    return node.isTextual();
  }

  public boolean isArray() {
    return node.isArray();
  }

  public boolean isNumber() {
    return node.isNumber();
  }

  public boolean isNull() {
    return node.isNull();
  }

  public boolean isIntegralNumber() {
    return node.isIntegralNumber();
  }

  public boolean isDecimalNumber() {
    return node.isFloatingPointNumber();
  }

  public boolean isObject() {
    return node.isObject();
  }

  public boolean isBoolean() {
    return node.isBoolean();
  }

  public String asString() {
    return node.asText();
  }

  public int asInt() {
    return node.asInt();
  }

  public long asLong() {
    return node.asLong();
  }

  public double asDouble() {
    return node.asDouble();
  }

  public boolean asBoolean() {
    return node.asBoolean();
  }

  public boolean has(String key) {
    return node.has(key);
  }

  public JsonValue get(String key) {
    JsonNode v = node.get(key);
    return v != null ? new JacksonJsonValue(v) : null;
  }

  public JsonValue get(int ix) {
    return new JacksonJsonValue(node.get(ix));
  }

  public int size() {
    return node.size();
  }

  public Iterator<String> getKeys() {
    return node.fieldNames();
  }

  public PairIterator pairIterator() {
    return new JacksonPairIterator();
  }

  public int hashCode() {
    return node.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof JacksonJsonValue) {
      return ((JacksonJsonValue) other).node.equals(node);
    } else if (other instanceof JsonValue) {
      JsonValue v = (JsonValue) other;
      return v.equals(this);
    } else
      return false;
  }

  public String toString() {
    return node.toString();
  }

  public void traverse(JsonEventHandler handler) {
    throw new UnsupportedOperationException();
  }

  // ===== PAIR ITERATOR

  class JacksonPairIterator implements PairIterator {
    private Iterator<Map.Entry<String,JsonNode>> iterator;
    private Map.Entry<String,JsonNode> entry;

    public JacksonPairIterator() {
      this.iterator = node.fields();
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public String key() {
      return entry.getKey();
    }

    public JsonValue value() {
      return new JacksonJsonValue(entry.getValue());
    }

    public void next() {
      this.entry = iterator.next();
    }
  }
}
