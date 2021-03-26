
package com.schibsted.spt.data.json;

import com.schibsted.spt.data.jslt.JsltException;

public class JsonBuilderHandler implements JsonEventHandler {
  private StackItem current;

  public JsonValue get() {
    return current.value;
  }

  public void handleString(String value) {
    newValue(new StringJValue(value));
  }

  public void handleLong(long value) {
    newValue(new LongJValue(value));
  }

  public void handleDouble(double value) {
    newValue(new DoubleJValue(value));
  }

  public void handleBoolean(boolean value) {
    newValue(value ? BooleanJValue.TRUE : BooleanJValue.FALSE);
  }

  public void handleNull() {
    newValue(NullJValue.instance);
  }

  public void startObject() {
    current = new StackItem(current, new OptimizedObjectBuilder());
  }

  public void handleKey(String key) {
    current.key = key;
  }

  public void endObject() {
    JsonValue value = current.object.build();
    if (current.parent == null)
      current.value = value;
    else {
      current = current.parent;
      newValue(value);
    }
  }

  public void startArray() {
    current = new StackItem(current);
  }

  public void endArray() {
    JsonValue value = new FixedArrayJValue(current.array, current.arrayPos);
    if (current.parent == null)
      current.value = value;
    else {
      current = current.parent;
      newValue(value);
    }
  }

  private void newValue(JsonValue value) {
    if (current == null)
      current = new StackItem(value);
    else if (current.key != null) {
      current.object.set(current.key, value);
      current.key = null;
    } else if (current.array != null)
      current.addArrayValue(value);
    else
      throw new JsltException("WTF");
  }

  private static class StackItem {
    JsonValue value;
    StackItem parent;

    JsonValue[] array;
    int arrayPos;
    JsonObjectBuilder object;
    String key;

    StackItem(JsonValue value) {
      this.value = value;
    }

    StackItem(StackItem parent, JsonObjectBuilder object) {
      this.parent = parent;
      this.object = object;
    }

    // this means we're an array
    StackItem(StackItem parent) {
      this.parent = parent;
      this.array = new JsonValue[10];
    }

    public void addArrayValue(JsonValue value) {
      if (arrayPos == array.length) {
        JsonValue[] tmp = new JsonValue[array.length * 2];
        System.arraycopy(array, 0, tmp, 0, array.length);
        array = tmp;
      }
      array[arrayPos++] = value;
    }
  }
}
