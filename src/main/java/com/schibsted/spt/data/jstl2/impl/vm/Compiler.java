
package com.schibsted.spt.data.jstl2.impl.vm;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.schibsted.spt.data.jstl2.Function;
import com.schibsted.spt.data.jstl2.Expression;
import com.schibsted.spt.data.jstl2.impl.LetExpression;
import com.schibsted.spt.data.jstl2.impl.ExpressionImpl;

public class Compiler {
  private List<Integer> bytecode;
  private ValuePool<JsonNode> literals;
  private ValuePool<String> variables;
  private ValuePool<Function> functions;

  private static final Map<String, Integer> operatorCode = new HashMap();
  static {
    operatorCode.put("==", VirtualMachine.OP_EQUAL);
    operatorCode.put("!=", VirtualMachine.OP_NEQ);
    operatorCode.put("+", VirtualMachine.OP_PLUS);
    operatorCode.put(">=", VirtualMachine.OP_GTEQ);
    operatorCode.put("and", VirtualMachine.OP_AND);
    operatorCode.put("or", VirtualMachine.OP_OR);
  }

  public Compiler() {
    this.bytecode = new ArrayList();
    this.literals = new ValuePool<JsonNode>();
    this.variables = new ValuePool<String>();
    this.functions = new ValuePool<Function>();
  }

  public Expression compile(ExpressionImpl root) {
    root.compile(this);
    return buildImage();
  }

  public Expression buildImage() {
    return new VirtualMachineImage(bytecode, literals, variables, functions);
  }

  public void compileLets(LetExpression[] lets) {
    if (lets.length > 0)
      genPUSHS();
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].compile(this);
  }

  public int getNextInstruction() {
    return bytecode.size();
  }

  public void generateOperatorCode(String operator) {
    bytecode.add(operatorCode.get(operator));
    bytecode.add(0);
  }

  public void genPUSHO() {
    bytecode.add(VirtualMachine.OP_PUSHO);
    bytecode.add(0);
  }

  public void genPUSHA() {
    bytecode.add(VirtualMachine.OP_PUSHA);
    bytecode.add(0);
  }

  public void genPUSHI() {
    bytecode.add(VirtualMachine.OP_PUSHI);
    bytecode.add(0);
  }

  public void genPUSHL(JsonNode literal) {
    bytecode.add(VirtualMachine.OP_PUSHL);
    bytecode.add(literals.getIndex(literal));
  }

  public void genPUSHV(String variable) {
    bytecode.add(VirtualMachine.OP_PUSHV);
    bytecode.add(variables.getIndex(variable));
  }

  public void genPUSHS() {
    bytecode.add(VirtualMachine.OP_PUSHS);
    bytecode.add(0);
  }

  public void genPOPS() {
    bytecode.add(VirtualMachine.OP_POPS);
    bytecode.add(0);
  }

  public void genDOT(String key) {
    bytecode.add(VirtualMachine.OP_DOT);
    bytecode.add(literals.getIndex(new TextNode(key)));
  }

  public Jump genJNOT() {
    bytecode.add(VirtualMachine.OP_JNOT);
    bytecode.add(0);
    return new Jump(bytecode);
  }

  public void genJUMP(int dest) {
    bytecode.add(VirtualMachine.OP_JUMP);
    bytecode.add(dest);
  }

  public Jump genJUMP() {
    bytecode.add(VirtualMachine.OP_JUMP);
    bytecode.add(0);
    return new Jump(bytecode);
  }

  public void genSETK(String key) {
    bytecode.add(VirtualMachine.OP_SETK);
    bytecode.add(literals.getIndex(new TextNode(key)));
  }

  public void genDSETK() {
    bytecode.add(VirtualMachine.OP_DSETK);
    bytecode.add(0);
  }

  public void genSETA() {
    bytecode.add(VirtualMachine.OP_SETA);
    bytecode.add(0);
  }

  public void genCALL(Function fun) {
    bytecode.add(VirtualMachine.OP_CALL);
    bytecode.add(functions.getIndex(fun));
  }

  public void genSTORE(String variable) {
    bytecode.add(VirtualMachine.OP_STORE);
    bytecode.add(variables.getIndex(variable));
  }

  public void genAIDX() {
    bytecode.add(VirtualMachine.OP_AIDX);
    bytecode.add(0);
  }

  public void genASLC() {
    bytecode.add(VirtualMachine.OP_ASLC);
    bytecode.add(0);
  }

  public void genPOP() {
    genPOP(1); // default: pop one element
  }

  public void genPOP(int elements) {
    bytecode.add(VirtualMachine.OP_POP);
    bytecode.add(elements);
  }

  public void genSWAP() {
    bytecode.add(VirtualMachine.OP_SWAP);
    bytecode.add(0);
  }

  public void genPOPI() {
    bytecode.add(VirtualMachine.OP_POPI);
    bytecode.add(0);
  }

  public void genDUP() {
    bytecode.add(VirtualMachine.OP_DUP);
    bytecode.add(0);
  }

  public void genALD() {
    bytecode.add(VirtualMachine.OP_ALD);
    bytecode.add(0);
  }

  public void genOLD() {
    bytecode.add(VirtualMachine.OP_OLD);
    bytecode.add(0);
  }

  public void genDEBUG() {
    bytecode.add(VirtualMachine.OP_DEBUG);
    bytecode.add(0);
  }

  public void genEND() {
    bytecode.add(VirtualMachine.OP_END);
    bytecode.add(0);
  }
}
