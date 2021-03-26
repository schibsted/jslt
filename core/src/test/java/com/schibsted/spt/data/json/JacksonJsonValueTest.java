
package com.schibsted.spt.data.json;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JacksonJsonValueTest {
  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testLongComparison() {
    assertTrue(new JacksonJsonValue(new LongNode(42)).equals(new LongJValue(42)));
    assertTrue(new LongJValue(42).equals(new JacksonJsonValue(new LongNode(42))));
  }

  @Test
  public void testSelfComparison() {
    JsonValue v = new JacksonJsonValue(new LongNode(42));
    assertTrue(v.equals(v));
  }

  @Test
  public void testNullComparison() {
    assertTrue(new JacksonJsonValue(NullNode.instance).equals(NullJValue.instance));
    assertTrue(NullJValue.instance.equals(new JacksonJsonValue(NullNode.instance)));
  }

  @Test
  public void testStringComparison() {
    assertTrue(new JacksonJsonValue(new TextNode("foo")).equals(new StringJValue("foo")));
    assertTrue(new StringJValue("foo").equals(new JacksonJsonValue(new TextNode("foo"))));
  }

  @Test
  public void testDoubleComparison() {
    assertTrue(new JacksonJsonValue(new DoubleNode(3.14)).equals(new DoubleJValue(3.14)));
    assertTrue(new DoubleJValue(3.14).equals(new JacksonJsonValue(new DoubleNode(3.14))));
  }

  @Test
  public void testBooleanComparison() {
    assertTrue(new JacksonJsonValue(BooleanNode.TRUE).equals(BooleanJValue.TRUE));
    assertTrue(BooleanJValue.TRUE.equals(new JacksonJsonValue(BooleanNode.TRUE)));
  }

  @Test
  public void testObjectComparison() {
    ObjectNode on = mapper.createObjectNode();
    on.put("foo", new TextNode("bar"));
    on.put("baz", new LongNode(42));
    JacksonJsonValue jjv = new JacksonJsonValue(on);

    DynamicJObject v = new DynamicJObject();
    v.set("foo", new StringJValue("bar"));
    v.set("baz", new LongJValue(42));

    assertTrue(jjv.isObject());
    assertEquals(jjv.size(), v.size());

    Iterator<String> it = v.getKeys();
    while (it.hasNext()) {
      String key = it.next();
      assertEquals(jjv.get(key), v.get(key));
    }

    assertTrue(jjv.equals(v));
    assertTrue(v.equals(jjv));
  }

  @Test
  public void testObjectIteration() {
    ObjectNode on = mapper.createObjectNode();
    on.put("foo", new TextNode("bar"));
    on.put("baz", new LongNode(42));
    JacksonJsonValue jjv = new JacksonJsonValue(on);

    Set<String> keys = new HashSet<>();
    Iterator<String> it = jjv.getKeys();
    while (it.hasNext())
      keys.add(it.next());

    assertEquals(2, keys.size());
    assertTrue(keys.contains("foo"));
    assertTrue(keys.contains("baz"));

    keys = new HashSet<>();
    Set<JsonValue> values = new HashSet<>();
    PairIterator pit = jjv.pairIterator();
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
