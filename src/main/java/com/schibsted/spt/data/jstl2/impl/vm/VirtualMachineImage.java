
package com.schibsted.spt.data.jstl2.impl.vm;

import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.Expression;
import com.schibsted.spt.data.jstl2.JstlException;

public class VirtualMachineImage implements Expression {
  private int[] bytecode;
  private JsonNode[] literals;
  private ValuePool<String> allVariables;
  private JsonNode[] emptyVars;
  private Function[] functions;

  public VirtualMachineImage(List<Integer> code,
                             ValuePool<JsonNode> literals,
                             ValuePool<String> variables,
                             ValuePool<Function> functions) {
    System.out.println("bytecode " + code);
    this.bytecode = new int[code.size()];
    for (int ix = 0; ix < bytecode.length; ix++)
      this.bytecode[ix] = code.get(ix);
    System.out.println("bytecode.length " + bytecode.length);

    this.literals = literals.toArray(JsonNode.class);
    this.functions = functions.toArray(Function.class);

    this.allVariables = variables;
    this.emptyVars = new JsonNode[allVariables.size()];
  }

  public JsonNode apply(JsonNode input) {
    VirtualMachine vm = new VirtualMachine(emptyVars, literals, functions, bytecode);
    return vm.apply(input);
  }

  public JsonNode apply(Map<String, JsonNode> variables, JsonNode input) {
    JsonNode[] vars = new JsonNode[allVariables.size()];
    for (String var : variables.keySet())
      vars[allVariables.getIndex(var)] = variables.get(var);
    return new VirtualMachine(vars, literals, functions, bytecode).apply(input);
  }

}
