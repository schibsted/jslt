
package com.schibsted.spt.data.jstl2.impl.vm;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.HashMap;

/**
 * Helper class which assigns numeric indexes to unique values, then
 * returns an array of them at the end.
 */
public class ValuePool<T> {
  private Map<T, Integer> pool;

  public ValuePool() {
    this.pool = new HashMap<T, Integer>();
  }

  public int getIndex(T value) {
    Integer ix = pool.get(value);
    if (ix == null) {
      ix = pool.size();
      pool.put(value, ix);
    }
    return ix;
  }

  public T[] toArray(Class<T> klass) {
    T[] array = (T[]) Array.newInstance(klass, pool.size());
    for (Map.Entry<T, Integer> e : pool.entrySet())
      array[e.getValue()] = e.getKey();
    return array;
  }

  public int size() {
    return pool.size();
  }
}
