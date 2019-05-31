
package com.schibsted.spt.data.jslt.vm;

import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public class JsonBuffer {
  // tags for recognizing object types in int values
  public static final int JS_INTEGER = 0x80000000; // 1xxxxxxxx
  public static final int JS_DECIMAL = 0x40000000; // 01xxxxxxx
  public static final int JS_STRING  = 0x20000000; // 001xxxxxx
  public static final int JS_OBJECT  = 0x10000000; // 0001xxxxx
  public static final int JS_ARRAY   = 0x08000000; // 00001xxxx
  public static final int JS_BIGINT  = 0x04000000; // 000001xxx
  public static final int JS_BOOLEAN = 0x01000000; // 00000001x
  public static final int JS_NULL    = 0x00700000; // 000000001

  // mask for removing the type tag
  public static final int RM_INTEGER = 0x7FFFFFFF;
  public static final int RM_STRING  = 0x1FFFFFFF;
  public static final int RM_OBJECT  = 0x0FFFFFFF;
  public static final int RM_ARRAY   = 0x07FFFFFF;
  public static final int RM_BIGINT  = 0x03FFFFFF;
  public static final int RM_BOOLEAN = 0x00FFFFFF;

  // where the data is stored
  private int[] data;
  private int end; // next free cell

  // if we can get rid of these, the performance impact would be
  // noticeable. one way to do it might be to just have a pointer to
  // the last opened object. when a new object is opened, put the
  // pointer into that object, and update the pointer. whenever an
  // object is closed, the pointer can be retrieved. in this way we
  // can emulate a stack without actually having to maintain one.
  private int[] stack;
  private int stack_ix;

  public JsonBuffer() {
    this.data = new int[8];
    this.end = 0;

    this.stack = new int[8];
    this.stack_ix = 0;
  }

  // === BUILD JSON

  public void startObject() {
    expandIfNecessary();
    stack[stack_ix++] = end;
    data[end++] = JS_OBJECT;
  }

  public void endObject() {
    // end is where the next thing, whatever it is, will be
    // our object is on top of the stack, so get reference from there
    // then AND in reference to next thing
    data[stack[--stack_ix]] = JS_OBJECT | end;
  }

  public void addString(int id) {
    expandIfNecessary();
    data[end++] = JS_STRING | id;
  }

  public void addInlineInt(int value) {
    expandIfNecessary();
    data[end++] = JS_INTEGER | value;
  }

  public void addBigInt(int id) {
    expandIfNecessary();
    data[end++] = JS_BIGINT | id;
  }

  public void startArray() {
    expandIfNecessary();
    stack[stack_ix++] = end;
    data[end++] = JS_ARRAY;
  }

  public void endArray() {
    // end is where the next thing, whatever it is, will be
    // our array is on top of the stack, so get reference from there
    // then AND in reference to next thing
    data[stack[--stack_ix]] = JS_ARRAY | end;
  }

  public void addBoolean(boolean b) {
    expandIfNecessary();
    data[end++] = JS_BOOLEAN | (b ? 1 : 0);
  }

  public void addRawValue(int value) {
    expandIfNecessary();
    data[end++] = value;
  }

  public void addNull() {
    expandIfNecessary();
    data[end++] = JS_NULL;
  }

  // FIXME: this is broken
  public void addDecimal(float number) {
    expandIfNecessary();
    data[end++] = JS_DECIMAL | ((int) number);
  }

  private void expandIfNecessary() {
    if (end == data.length) {
      int[] newData = new int[data.length * 2];
      System.arraycopy(data, 0, newData, 0, data.length);
      data = newData;
    }
  }

  // ===== OUTPUT TO JSON

  public void streamTo(JsonGenerator gen, ResourceManager mgr) throws IOException {
    streamTo(gen, 0, mgr);
    gen.flush();
  }

  public int streamTo(JsonGenerator gen, int ix, ResourceManager mgr) throws IOException {
    if ((data[ix] & JS_INTEGER) == JS_INTEGER) {
      gen.writeNumber(data[ix++] & RM_INTEGER);
    } else if ((data[ix] & JS_STRING) == JS_STRING) {
      gen.writeString(mgr.getString(data[ix++] & RM_STRING));
    } else if ((data[ix] & JS_OBJECT) == JS_OBJECT) {
      int end = data[ix] & RM_OBJECT;
      gen.writeStartObject();
      ix++;

      while (ix < end) {
        gen.writeFieldName(mgr.getString(data[ix] & RM_STRING));
        ix = streamTo(gen, ix + 1, mgr);
      }
      gen.writeEndObject();
    } else if ((data[ix] & JS_ARRAY) == JS_ARRAY) {
      gen.writeStartArray();
      ix++;

      int end = data[ix] & RM_ARRAY;
      while (ix < end)
        ix = streamTo(gen, ix, mgr);

      gen.writeEndArray();
    } else if ((data[ix] & JS_BIGINT) == JS_BIGINT) {
      gen.writeNumber(mgr.getBigInt(data[ix++] & RM_BIGINT));
    } else if ((data[ix] & JS_BOOLEAN) == JS_BOOLEAN) {
      gen.writeBoolean((data[ix++] & JS_BOOLEAN) == 1);
    } else if (data[ix] == JS_NULL) {
      gen.writeNull();
      ix++;
    } else
      throw new RuntimeException("Unknown object type (" + ix + "): " + data[ix]);

    return ix;
  }

  // ===== QUERY

  public int getOffset() {
    return end;
  }

  public int getValueAt(int offset) {
    return data[offset];
  }

  // key is (JS_INTEGER | id) of the key string we seek.
  // ix is the offset of the object start
  // return value is ... what, exactly?
  public int getKey(int key, int ix) {
    int end = data[ix] & RM_OBJECT;
    ix++; // step over the start object marker

    while (ix < end && data[ix] != key) {
      int value = data[ix+1];
      if ((value & JS_OBJECT) == JS_OBJECT) {
        ix = value & RM_OBJECT;
      } else if ((value & JS_ARRAY) == JS_ARRAY) {
        ix = value & RM_ARRAY;
      } else {
        ix += 2; // it's an inline value, so just step over
      }
    }

    if (data[ix] == key) {
      int value = data[ix+1];
      if ((value & JS_OBJECT) == JS_OBJECT)
        return JS_OBJECT | (ix+1); // caller will add buffer ix
      else if ((value & JS_ARRAY) == JS_ARRAY)
        return JS_ARRAY | (ix+1);  // caller will add buffer ix
      else
        return data[ix+1]; // literals we can just return
    } else
      return JS_NULL;
  }

  // true iff object or array
  public static boolean isContainer(int value) {
    return (((value & JS_OBJECT) == JS_OBJECT) ||
            ((value & JS_ARRAY) == JS_ARRAY));
  }

  // ===== DUMP

  public void dump() {
    System.out.println("===== buffer dump start =====");
    for (int ix = 0; ix < end; ix++) {
      System.out.println("buf[" + ix + "] = " + data[ix]);
    }
    System.out.println("===== buffer dump end =====");
  }
}
