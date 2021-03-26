
package com.schibsted.spt.data.json;

import java.util.Iterator;
import com.schibsted.spt.data.jslt.JsltException;

public class JsonTraversal {

  public static void traverse(JsonValue value, JsonEventHandler handler) {
    if (value.isString())
      handler.handleString(value.asString());
    else if (value.isObject())
      traverseObject(value, handler);
    else if (value.isArray())
      traverseArray(value, handler);
    else if (value.isIntegralNumber())
      handler.handleLong(value.asLong());
    else if (value.isDecimalNumber())
      handler.handleDouble(value.asDouble());
    else if (value.isBoolean())
      handler.handleBoolean(value.asBoolean());
    else if (value.isNull())
      handler.handleNull();
    else
      throw new JsltException("Can't traverse " + value);
  }

  private static void traverseObject(JsonValue value, JsonEventHandler handler) {
    handler.startObject();
    PairIterator it = value.pairIterator();
    while (it.hasNext()) {
      it.next();
      handler.handleKey(it.key());
      traverse(it.value(), handler);
    }
    handler.endObject();
  }

  private static void traverseArray(JsonValue value, JsonEventHandler handler) {
    handler.startArray();
    for (int ix = 0; ix < value.size(); ix++)
      traverse(value.get(ix), handler);
    handler.endArray();
  }

}
