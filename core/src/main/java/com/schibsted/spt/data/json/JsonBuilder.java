
package com.schibsted.spt.data.json;

import java.util.List;

public interface JsonBuilder {

  public JsonValue makeValue(boolean value);

  public JsonValue makeValue(long value);

  public JsonValue makeValue(double value);

  public JsonValue makeValue(String value);

  public JsonValue makeArray(JsonValue[] buffer);

  public JsonValue makeArray(JsonValue[] buffer, int length);

  public JsonValue makeArray(List<JsonValue> values);

  public NullJValue makeNull();

  public BooleanJValue makeTrue();

  public BooleanJValue makeFalse();

  public JsonObjectBuilder makeObjectBuilder();

}
