
package com.schibsted.spt.data.json;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import com.schibsted.spt.data.jslt.JsltException;

public abstract class AbstractJsonValue implements JsonValue {

  // ===== JsonValue

  public boolean isSequence() {
    return false;
  }

  public boolean isString() {
    return false;
  }

  public boolean isArray() {
    return false;
  }

  public boolean isNumber() {
    return false;
  }

  public boolean isNull() {
    return false;
  }

  public boolean isIntegralNumber() {
    return false;
  }

  public boolean isDecimalNumber() {
    return false;
  }

  public boolean isObject() {
    return false;
  }

  public boolean isBoolean() {
    return false;
  }

  public String asString() {
    throw new JsltException("Cannot convert " + this + " to string");
  }

  public int asInt() {
    throw new JsltException("Cannot convert " + this + " to int");
  }

  public long asLong() {
    throw new JsltException("Cannot convert " + this + " to long");
  }

  public double asDouble() {
    throw new JsltException("Cannot convert " + this + " to double");
  }

  public boolean asBoolean() {
    throw new JsltException("Cannot convert " + this + " to boolean");
  }

  public boolean has(String key) {
    return false;
  }

  public JsonValue get(String key) {
    return makeNull();
  }

  public JsonValue get(int ix) {
    return makeNull();
  }

  public int size() {
    return -1;
  }

  public Iterator<String> getKeys() {
    return Collections.emptyIterator();
  }

  // ===== JsonBuilder

  public JsonValue makeValue(long value) {
    // FIXME: make a common values cache
    return new LongJValue(value);
  }

  public JsonValue makeValue(double value) {
    return new DoubleJValue(value);
  }

  public JsonValue makeValue(String value) {
    return new StringJValue(value);
  }

  public JsonValue makeArray(JsonValue[] buffer) {
    return new FixedArrayJValue(buffer, buffer.length);
  }

  public JsonValue makeArray(JsonValue[] buffer, int length) {
    return new FixedArrayJValue(buffer, length);
  }

  public JsonValue makeArray(List<JsonValue> values) {
    return null;
  }

  public NullJValue makeNull() {
    return NullJValue.instance;
  }

  public JsonValue makeValue(boolean value) {
    if (value)
      return BooleanJValue.TRUE;
    else
      return BooleanJValue.FALSE;
  }

  public BooleanJValue makeTrue() {
    return BooleanJValue.TRUE;
  }

  public BooleanJValue makeFalse() {
    return BooleanJValue.FALSE;
  }

  public JsonObjectBuilder makeObjectBuilder() {
    return new DynamicJObject();
  }

}
