
package com.schibsted.spt.data.jstl2.impl.vm;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.NodeUtils;

public class VirtualMachine {
  private int var_count; // number of variables
  private JsonNode[][] variables;
  private JsonNode[] literals;
  private Function[] functions;
  private int[] bytecode;
  private JsonNode[] stack;

  private int pc;  // next opcode
  private int stp; // stack pointer
  private int scp; // current scope frame (variables)

  private JsonNode input;
  private ObjectMapper mapper = NodeUtils.mapper;

  public VirtualMachine(JsonNode[] variables, JsonNode[] literals,
                        Function[] functions, int[] bytecode) {
    this.var_count = variables.length;
    this.variables = new JsonNode[24][];
    this.variables[0] = variables;
    this.literals = literals;
    this.functions = functions;
    this.bytecode = bytecode;
    this.stack = new JsonNode[1024];
    this.stp = -1;
    // pc = 0, as it should be
    // scp = 0, also as it should be
    // input gets set by apply
  }

  public JsonNode apply(JsonNode input) {
    this.input = input;
    return run();
  }

  public JsonNode run() {
    while (true) {
      int param = bytecode[pc + 1];
      System.out.println("run: " + bytecode[pc] +" " + param);
      switch(bytecode[pc]) {
      case OP_PUSHO:
        stack[++stp] = mapper.createObjectNode();
        break;
      case OP_PUSHA:
        stack[++stp] = mapper.createArrayNode();
        break;
      case OP_PUSHL:
        stack[++stp] = literals[param];
        break;
      case OP_PUSHS:
        variables[++scp] = new JsonNode[var_count];
        break;
      case OP_PUSHI:
        stack[++stp] = input;
        break;
      case OP_PUSHV:
        int stf = scp;
        JsonNode val = variables[stf][param];
        while (val == null)
          val = variables[--stf][param];
        stack[++stp] = val;
        break;
      case OP_DOT:
        val = stack[stp].get(literals[param].asText());
        if (val == null)
          val = NullNode.instance;
        stack[stp] = val;
        break;
      case OP_AIDX:
        int ix = stack[stp--].intValue();
        ArrayNode array = (ArrayNode) stack[stp--];
        stack[++stp] = array.get(resolveIndex(ix, array.size()));
        break;
      case OP_ASLC:
        val = stack[stp--]; // right
        int i1 = stack[stp--].intValue();
        JsonNode v2 = stack[stp--];

        int i2 = v2.size();
        i1 = resolveIndex(i1, i2);
        if (!val.isNull())
          i2 = Math.min(resolveIndex(val.intValue(), i2), i2);

        array = mapper.createArrayNode();
        for (ix = i1; ix < i2; ix++)
          array.add(v2.get(ix));
        stack[++stp] = array;

        break;
      case OP_SETK:
        val = stack[stp--];
        if (NodeUtils.isValue(val))
          ((ObjectNode) stack[stp]).set(literals[param].asText(), val);
        break;
      case OP_SETA:
        val = stack[stp--];
        ((ArrayNode) stack[stp]).add(val);
        break;
      case OP_STORE:
        variables[scp][param] = stack[stp--];
        break;
      case OP_DUP:
        stack[stp + 1] = stack[stp];
        stp++;
        break;
      case OP_POPS:
        scp--;
        break;
      case OP_JNOT:
        if (!NodeUtils.isTrue(stack[stp--])) {
          pc = param;
          continue; // don't do +2
        }
        break;
      case OP_JUMP:
        pc = param;
        continue; // don't do +2
      case OP_CALL:
        Function func = functions[param];
        int paramCount = stack[stp--].intValue();
        JsonNode[] params = new JsonNode[paramCount];
        for (ix = 0; ix < paramCount; ix++)
          params[ix] = stack[stp--];
        stack[++stp] = func.call(input, params);
        break;
      case OP_EQUAL:
        val = stack[stp--];
        v2 = stack[stp--];
        stack[++stp] = NodeUtils.toJson(val.equals(v2));
        break;
      case OP_NEQ:
        val = stack[stp--];
        v2 = stack[stp--];
        stack[++stp] = NodeUtils.toJson(!val.equals(v2));
        break;
      case OP_PLUS:
        String s2 = NodeUtils.toString(stack[stp--], false); // right
        String s1 = NodeUtils.toString(stack[stp--], false); // left
        stack[++stp] = new TextNode(s1 + s2);
        break;
      case OP_GTEQ:
        i2 = NodeUtils.number(stack[stp--]).intValue(); // right
        i1 = NodeUtils.number(stack[stp--]).intValue(); // left
        stack[++stp] = NodeUtils.toJson(i1 >= i2);
        break;
      case OP_AND:
        boolean b1 = NodeUtils.isTrue(stack[stp--]);
        boolean b2 = NodeUtils.isTrue(stack[stp--]);
        stack[++stp] = NodeUtils.toJson(b1 && b2);
        break;
      case OP_OR:
        b1 = NodeUtils.isTrue(stack[stp--]);
        b2 = NodeUtils.isTrue(stack[stp--]);
        stack[++stp] = NodeUtils.toJson(b1 || b2);
        break;
      case OP_END:
        if (stp != 0)
          throw new JstlException("Inconsistent stack!");
        return stack[0];
      default:
        throw new JstlException("Unimplemented opcode " + bytecode[pc]);
      }
      pc += 2;
    }
  }

  private static final int resolveIndex(int ix, int size) {
    if (ix < 0)
      return size + ix;
    else
      return ix;
  }

  public static final int OP_PUSHO = 0;
  public static final int OP_PUSHL = 1;
  public static final int OP_PUSHS = 2;
  public static final int OP_PUSHI = 3;
  public static final int OP_DOT   = 4;
  public static final int OP_STORE = 5;
  public static final int OP_DUP   = 6;
  public static final int OP_POPS  = 7;
  public static final int OP_END   = 8;
  public static final int OP_JNOT  = 9;
  public static final int OP_JUMP  = 10;
  public static final int OP_SETK  = 11;
  public static final int OP_PUSHV = 12;
  public static final int OP_CALL  = 13;
  public static final int OP_PUSHA = 14;
  public static final int OP_SETA  = 15;
  public static final int OP_EQUAL = 16;
  public static final int OP_NEQ   = 17;
  public static final int OP_PLUS  = 18;
  public static final int OP_GTEQ  = 19;
  public static final int OP_AND   = 20;
  public static final int OP_OR    = 21;
  public static final int OP_AIDX  = 22;
  public static final int OP_ASLC  = 23;
}
