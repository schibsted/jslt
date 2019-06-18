
package com.schibsted.spt.data.jslt.vm;

/**
 * Created every time we need to execute a query. Lives only for the
 * lifetime of that query. Unfortunately, creating this is expensive,
 * so we should find some way to avoid having to create new ones all
 * the time.
 */
public class JsonVm {
  // passed to constructor
  private ResourceManager mgr;
  private int[] bytecode;

  // internals
  private JsonBuffer[] buffers;
  private int buffers_ptr; // points to current buffer
  private int[] stack;
  private int stack_ptr; // points to current value

  private int context_buf; // points to the buffer that is the context object
  private int context_off; // gives the offset of the context obj within buffer

  // variable slots?
  // function list?

  // opcodes
  public static final int START_BUF = 0;
  public static final int START_OBJ = 1;
  public static final int GET_KEY   = 2;
  public static final int PUSH_CTX  = 3;
  public static final int STOP      = 4;
  public static final int END_OBJ   = 5;
  public static final int SET_KEY   = 6;

  // object tag is 0001xxxxx... that means 28 bits remain. inside
  // JsonBuffer all of those are used for the offset to the next
  // value.  here one byte refers to the buffer, and the remaining 20
  // bits are the offset within the buffer.
  //
  // tag = y, buffer ptr = x, buffer offset = z ->
  // yyyyxxxxxxxxzzzzzzzzzzzzzzzzzzzz
  //
  // public static final int JS_OBJECT  = 0x10000000;
  //
  public static final int JS_OBJ_BUFPTR_MASK = 0x0FF00000; // get buf ptr
  public static final int JS_OBJ_BUFOFF_MASK = 0x000FFFFF; // get buf offset

  public JsonVm(ResourceManager mgr) {
    this.mgr = mgr;

    this.buffers = new JsonBuffer[10];
    this.buffers_ptr = -1; // no current buffer

    this.stack = new int[128];
    this.stack_ptr = -1; // nothing on stack

    // hardcoded bytecode for a specific transform
    this.bytecode = new int[]{
      START_BUF, 0,
      START_OBJ, 0,
      PUSH_CTX, 0,
      GET_KEY, mgr.getStringId("@id"),
      SET_KEY, mgr.getStringId("event_id"),
      PUSH_CTX, 0,
      GET_KEY, mgr.getStringId("@type"),
      SET_KEY, mgr.getStringId("event_type"),
      PUSH_CTX, 0,
      GET_KEY, mgr.getStringId("actor"),
      GET_KEY, mgr.getStringId("spt:userId"),
      SET_KEY, mgr.getStringId("user_id"),
      END_OBJ, 0,
      STOP, 0
    };
  }

  public int execute(JsonBuffer input) {
    this.buffers_ptr = -1; // no current buffer
    this.stack_ptr = -1; // nothing on stack

    buffers[++buffers_ptr] = input; // input is always in buffer 0
    context_buf = 0; // point to the input
    context_off = 0; // point to the top-level value in input

    int pc = 0;
    while (bytecode[pc] != STOP) {
      switch (bytecode[pc]) {
      case START_BUF:
        buffers[++buffers_ptr] = new JsonBuffer();
        break;

      case START_OBJ:
        int offset = buffers[buffers_ptr].getOffset();
        stack[++stack_ptr] = makeObjectReference(buffers_ptr, offset);
        buffers[buffers_ptr].startObject();
        break;

      case PUSH_CTX:
        stack[++stack_ptr] = makeObjectReference(context_buf, context_off);
        break;

      case GET_KEY:
        // takes object from top of stack, replaces with result

        if ((JsonBuffer.JS_OBJECT & stack[stack_ptr]) == JsonBuffer.JS_OBJECT) {
          int buffer_ix = getBufferIndex(stack[stack_ptr]);
          int buffer_of = getBufferOffset(stack[stack_ptr]);
          int key = bytecode[pc + 1] | JsonBuffer.JS_STRING;
          stack[stack_ptr] = buffers[buffer_ix].getKey(key, buffer_of);

          // if this is an object or array we need to add buffer index
          if (JsonBuffer.isContainer(stack[stack_ptr]))
            stack[stack_ptr] = (buffer_ix << 20) | stack[stack_ptr];

        } else if (stack[stack_ptr] != JsonBuffer.JS_NULL)
          throw new RuntimeException("BAD OBJECT");

        break;

      case SET_KEY:
        // key value on top of stack, object reference below. consume value,
        // but leave the object
        int value = stack[stack_ptr--];
        int buffer_ix = getBufferIndex(stack[stack_ptr]);
        int buffer_of = getBufferOffset(stack[stack_ptr]);
        int key = bytecode[pc + 1];

        buffers[buffer_ix].addString(key); // first write the key
        // FIXME: it's not necessarily a literal!!
        buffers[buffer_ix].addRawValue(value);
        break;

      case END_OBJ:
        buffers[buffers_ptr].endObject();
        break;

      default:
        throw new RuntimeException("WHOA!");
      }

      pc += 2;
    }

    return stack[0];
  }

  public JsonBuffer getBuffer(int id) {
    return buffers[id];
  }

  private static int makeObjectReference(int buffer_ptr, int buffer_off) {
    // add together JS_OBJECT_TAG, buffer ptr, and buffer offset
    return JsonBuffer.JS_OBJECT | buffer_off | (buffer_ptr << 20);
  }

  public static int getBufferIndex(int obj) {
    return (obj & JS_OBJ_BUFPTR_MASK) >> 20;
  }

  public static int getBufferOffset(int obj) {
    return (obj & JS_OBJ_BUFOFF_MASK);
  }
}
