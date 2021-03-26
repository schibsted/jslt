
package com.schibsted.spt.data.json;

public class JValueUtils {

  public static boolean equalObjects(JsonValue obj1, JsonValue obj2) {
    if (obj1.isObject() && obj2.isObject() && obj1.size() == obj2.size()) {
      PairIterator it = obj1.pairIterator();
      while (it.hasNext()) {
        it.next();

        JsonValue v2 = obj2.get(it.key());
        if (!v2.equals(it.value()))
          return false;
      }
      return true;
    }
    return false;
  }

}
