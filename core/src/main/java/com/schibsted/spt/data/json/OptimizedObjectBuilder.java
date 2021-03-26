
package com.schibsted.spt.data.json;

public class OptimizedObjectBuilder implements JsonObjectBuilder {
  private DynamicJObject object;

  public OptimizedObjectBuilder() {
  }

  public JsonObjectBuilder set(String key, JsonValue value) {
    if (object == null)
      object = new DynamicJObject();
    object.set(key, value);
    return this;
  }

  public JsonObjectBuilder set(String key, String value) {
    if (object == null)
      object = new DynamicJObject();
    object.set(key, object.makeValue(value));
    return this;
  }

  public boolean has(String key) {
    return object != null && object.has(key);
  }

  public JsonValue build() {
    if (object == null)
      return EmptyJObject.EMPTY_OBJECT;
    else
      return object;
  }
}
