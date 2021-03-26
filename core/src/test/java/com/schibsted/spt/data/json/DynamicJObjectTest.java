
package com.schibsted.spt.data.json;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class DynamicJObjectTest {

  @Test
  public void testIteration() {
    DynamicJObject object = new DynamicJObject();
    object.set("foo", new StringJValue("bar"));
    object.set("baz", new LongJValue(42));

    Set<String> keys = new HashSet<>();
    Iterator<String> it = object.getKeys();
    while (it.hasNext())
      keys.add(it.next());

    assertEquals(2, keys.size());
    assertTrue(keys.contains("foo"));
    assertTrue(keys.contains("baz"));

    keys = new HashSet<>();
    Set<JsonValue> values = new HashSet<>();
    PairIterator pit = object.pairIterator();
    while (pit.hasNext()) {
      pit.next();
      keys.add(pit.key());
      values.add(pit.value());
    }

    assertEquals(2, keys.size());
    assertTrue(keys.contains("foo"));
    assertTrue(keys.contains("baz"));

    assertEquals(2, values.size());
    assertTrue(values.contains(new StringJValue("bar")));
    assertTrue(values.contains(new LongJValue(42)));
  }
}
