
package com.schibsted.spt.data.jstl2.impl;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Jump;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class ObjectExpression extends AbstractNode {
  private LetExpression[] lets;
  private PairExpression[] children;
  private DotExpression contextQuery; // find object to match
  private MatcherExpression matcher;
  private Set<String> keys; // the keys defined in this template

  public ObjectExpression(LetExpression[] lets,
                          PairExpression[] children,
                          MatcherExpression matcher,
                          Location location) {
    super(location);
    this.lets = lets;
    this.children = children;
    this.matcher = matcher;

    this.keys = new HashSet();
    for (int ix = 0; ix < children.length; ix++)
      keys.add(children[ix].getKey());
    if (matcher != null)
      for (String minus : matcher.getMinuses())
        keys.add(minus);
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    scope = NodeUtils.evalLets(scope, input, lets);

    ObjectNode object = NodeUtils.mapper.createObjectNode();
    for (int ix = 0; ix < children.length; ix++) {
      JsonNode value = children[ix].apply(scope, input);
      if (NodeUtils.isValue(value))
        object.put(children[ix].getKey(), value);
    }

    if (matcher != null)
      evaluateMatcher(scope, input, object);

    return object;
  }

  private void evaluateMatcher(Scope scope, JsonNode input, ObjectNode object) {
    // find the object to match against
    JsonNode context = contextQuery.apply(scope, input);
    if (context.isNull() && !context.isObject())
      return; // no keys to match against

    // then do the matching
    Iterator<Map.Entry<String, JsonNode>> it = context.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> pair = it.next();
      if (keys.contains(pair.getKey()))
        continue; // the template has defined this key, so skip

      JsonNode value = matcher.apply(scope, pair.getValue());
      object.put(pair.getKey(), value);
    }
  }

  public void computeMatchContexts(DotExpression parent) {
    if (matcher != null) {
      contextQuery = parent;
      contextQuery.checkOk(location); // verify expression is legal
    }

    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].computeMatchContexts(parent);
    for (int ix = 0; ix < children.length; ix++)
      children[ix].computeMatchContexts(parent);
  }

  public ExpressionNode optimize() {
    boolean allLiterals = matcher == null; // not static otherwise
    for (int ix = 0; ix < children.length; ix++) {
      children[ix] = (PairExpression) children[ix].optimize();
      allLiterals = allLiterals && children[ix].isLiteral();
    }
    if (!allLiterals)
      return this;

    // we're a static object expression. we can just make the object and
    // turn that into a literal, instead of creating it over and over
    // apply parameters: literals won't use scope or input, so...
    JsonNode object = apply(null, NullNode.instance);
    return new LiteralExpression(object, location);
  }

  public void compile(Compiler compiler) {
    compiler.compileLets(lets);

    compiler.genPUSHO();
    for (int ix = 0; ix < children.length; ix++) {
      children[ix].compile(compiler);
      compiler.genSETK(children[ix].getKey());
    }

    if (matcher != null)
      compileMatcher(compiler);

    if (lets.length > 0)
      compiler.genPOPS();
  }

  private void compileMatcher(Compiler compiler) {
    // to understand how this works, see vm-design.txt

    // okay, evaluate
    compiler.genPUSHI(); // save input on stack
    compiler.genSWAP();  // result object now on top, input below

    contextQuery.compile(compiler); // find object to match against
    compiler.genOLD(keys);          // smear filtered object contents onto stack

    int start = compiler.getNextInstruction();
    compiler.genDUP();   // duplicate key so we have it after the test
    Jump end = compiler.genJNOT(); // go to end if finished
    compiler.genSWAP();      // now: value, key, obj
    compiler.genPOPI();      // value is now the input, stack: key, obj

    matcher.compile(compiler); // compute value to insert
    // stack is now: newval, key, obj, ...
    compiler.genDSETK();       // insert into obj
    compiler.genJUMP(start);   // goto next element

    end.resolve();
    // <stack from top: terminator, result, saved input>
    compiler.genPOP();         // get rid of terminator
    compiler.genSWAP();        // stack now: <saved input, result>
    compiler.genPOPI();        // restore input from stack
    // <result now on top of stack>
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + '{');
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].dump(level + 1);
    for (int ix = 0; ix < children.length; ix++)
      children[ix].dump(level + 1);
    System.out.println(NodeUtils.indent(level) + '}');
  }
}
