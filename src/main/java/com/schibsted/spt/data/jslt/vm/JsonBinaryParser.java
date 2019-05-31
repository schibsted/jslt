
package com.schibsted.spt.data.jslt.vm;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonToken.*;

// this class doesn't make a lot of sense, and will disappear
public class JsonBinaryParser {
  private static final JsonFactory jsonFactory = new JsonFactory();

  public static void main(String[] argv) throws IOException {
    ResourceManager mgr = new ResourceManager();

    JsonBuffer buf = parse(new File(argv[0]), mgr);
    buf.streamTo(jsonFactory.createJsonGenerator(System.out), mgr);
    System.out.println();
    System.out.println();

    int keyid = mgr.getStringId("@id");
    System.out.println("keyid: " + keyid);
    System.out.println(buf.getKey(keyid | JsonBuffer.JS_STRING, 0));

    System.out.println();
    JsonVm vm = new JsonVm(mgr);
    int result = vm.execute(buf);

    if ((result & JsonBuffer.JS_OBJECT) == JsonBuffer.JS_OBJECT) {
      JsonBuffer out = vm.getBuffer(vm.getBufferIndex(result));
      out.dump();
      JsonGenerator gen = jsonFactory.createJsonGenerator(System.out);
      System.out.println("out offset: " + vm.getBufferOffset(result));
      out.streamTo(gen, vm.getBufferOffset(result), mgr);
      gen.flush();
      System.out.println();
    } else
      System.out.println("UH OH");
  }

  public static JsonBuffer parse(File input, ResourceManager mgr) throws IOException {
    return parse(jsonFactory.createParser(input), mgr);
  }

  public static JsonBuffer parse(String json, ResourceManager mgr) throws IOException {
    return parse(jsonFactory.createParser(json), mgr);
  }

  // populate a JsonBuffer from an event stream from JsonParser. the
  // interface it pushes to should be separated out as an own interface
  public static JsonBuffer parse(JsonParser parser, ResourceManager mgr) throws IOException {
    JsonBuffer buf = new JsonBuffer();

    JsonToken token = parser.nextToken();
    while (token != null) {
      switch (token) {
      case START_OBJECT:
        buf.startObject();
        break;
      case END_OBJECT:
        buf.endObject();
        break;
      case FIELD_NAME:
      case VALUE_STRING:
        buf.addString(mgr.getStringId(parser.getText()));
        break;
      case VALUE_NUMBER_INT:
        boolean ok = false;
        try {
          int value = parser.getIntValue();
          if (value <= JsonBuffer.RM_INTEGER) {
            buf.addInlineInt(value);
            ok = true;
          }
        } catch (JsonParseException e) {
        }

        if (!ok)
          buf.addBigInt(mgr.getBigIntId(parser.getBigIntegerValue()));

        break;
      case START_ARRAY:
        buf.startArray();
        break;
      case END_ARRAY:
        buf.endArray();
        break;
      case VALUE_TRUE:
        buf.addBoolean(true);
        break;
      case VALUE_FALSE:
        buf.addBoolean(false);
        break;
      case VALUE_NULL:
        buf.addNull();
        break;
      case VALUE_NUMBER_FLOAT:
        buf.addDecimal(parser.getFloatValue());
        break;
      default:
        throw new RuntimeException("Unknown token: " + token);
      }

      token = parser.nextToken();
    }

    return buf;
  }

  public static void output(OutputStream out, JsonVm vm, int ref, ResourceManager mgr) throws IOException {
    JsonBuffer buf = vm.getBuffer(vm.getBufferIndex(ref));
    JsonGenerator gen = jsonFactory.createJsonGenerator(out);
    buf.streamTo(gen, vm.getBufferOffset(ref), mgr);
  }
}
