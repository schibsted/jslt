
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.schibsted.spt.data.jstl2.impl.vm.Jump;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class IfExpression extends AbstractNode {
  private ExpressionNode test;
  private LetExpression[] thenlets;
  private ExpressionNode then;
  private LetExpression[] elselets;
  private ExpressionNode orelse;

  public IfExpression(ExpressionNode test,
                      LetExpression[] thenlets,
                      ExpressionNode then,
                      LetExpression[] elselets,
                      ExpressionNode orelse,
                      Location location) {
    super(location);
    this.test = test;
    this.thenlets = thenlets;
    this.then = then;
    this.elselets = elselets;
    this.orelse = orelse;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    if (NodeUtils.isTrue(test.apply(scope, input)))
      return then.apply(NodeUtils.evalLets(scope, input, thenlets), input);

    // test was false, so return null or else
    if (orelse != null)
      return orelse.apply(NodeUtils.evalLets(scope, input, elselets), input);
    else
      return NullNode.instance;
  }

  public void computeMatchContexts(DotExpression parent) {
    for (int ix = 0; ix < thenlets.length; ix++)
      thenlets[ix].computeMatchContexts(parent);
    then.computeMatchContexts(parent);
    if (orelse != null) {
      orelse.computeMatchContexts(parent);
      for (int ix = 0; ix < elselets.length; ix++)
        elselets[ix].computeMatchContexts(parent);
    }
  }

  public void compile(Compiler compiler) {
    test.compile(compiler);

    Jump theelse = compiler.genJNOT();
    compiler.compileLets(thenlets);
    then.compile(compiler);
    if (thenlets.length > 0)
      compiler.genPOPS();

    Jump done = compiler.genJUMP();
    theelse.resolve(); // jump to next instructions
    if (orelse != null) {
      compiler.compileLets(elselets);
      orelse.compile(compiler);
      if (elselets.length > 0)
        compiler.genPOPS();
    } else
      compiler.genPUSHL(NullNode.instance);
    done.resolve();
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + "if (");
    test.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ")");

    for (int ix = 0; ix < thenlets.length; ix++)
      thenlets[ix].dump(level + 1);
    then.dump(level + 1);

    if (orelse != null) {
      System.out.println(NodeUtils.indent(level) + "else");
      for (int ix = 0; ix < elselets.length; ix++)
        elselets[ix].dump(level + 1);
      orelse.dump(level + 1);
    }
  }
}
