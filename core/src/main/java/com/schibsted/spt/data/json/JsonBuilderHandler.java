
package com.schibsted.spt.data.json;

public class JsonBuilderHandler implements JsonEventHandler {
  private JsonValue root;

  public JsonValue get() {
    return root;
  }

  public void handleString(String value) {
    this.root = new StringJValue(value);
  }

  public void handleLong(long value) {
    this.root = new LongJValue(value);
  }

  public void handleDouble(double value) {
  }

  public void handleBoolean(boolean value) {
    this.root = (value ? BooleanJValue.TRUE : BooleanJValue.FALSE);
  }

  public void handleNull() {
    this.root = NullJValue.instance;
  }

  public void startObject() {
  }

  public void handleKey(String key) {
  }

  public void endObject() {
  }

  public void startArray() {
  }

  public void endArray() {
  }

}
