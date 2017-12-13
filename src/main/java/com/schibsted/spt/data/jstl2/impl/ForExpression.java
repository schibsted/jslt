
package com.schibsted.spt.data.jstl2.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.schibsted.spt.data.jstl2.JstlException;
import com.schibsted.spt.data.jstl2.impl.vm.Jump;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class ForExpression extends AbstractNode {
  private ExpressionNode valueExpr;
  private ExpressionNode loopExpr;

  public ForExpression(ExpressionNode valueExpr,
                       ExpressionNode loopExpr,
                       Location location) {
    super(location);
    this.valueExpr = valueExpr;
    this.loopExpr = loopExpr;
  }

  public JsonNode apply(Scope scope, JsonNode input) {
    JsonNode array = valueExpr.apply(scope, input);
    if (array.isNull())
      return NullNode.instance;
    if (!array.isArray())
      throw new JstlException("For loop can't iterate over " + array, location);

    ArrayNode result = NodeUtils.mapper.createArrayNode();
    for (int ix = 0; ix < array.size(); ix++) {
      JsonNode value = array.get(ix);
      result.add(loopExpr.apply(scope, value));
    }
    return result;
  }

  public void computeMatchContexts(DotExpression parent) {
    // if you do matching inside a for the matching is on the current
    // object being traversed in the list. so we forget the parent
    // and start over
    loopExpr.computeMatchContexts(new DotExpression(location));
  }

  public void compile(Compiler compiler) {
    compiler.genPUSHI(); // save the input on the stack
    valueExpr.compile(compiler); // compute array to traverse

    compiler.genDUP(); // verify there actually is an array there
    Jump bitterend = compiler.genJNOT();

    compiler.genALD();   // load array to stack

    int start = compiler.getNextInstruction(); // remember this point
    Jump end = compiler.genJNOT();
    compiler.genPOPI();  // make current element be input

    loopExpr.compile(compiler); // insert code for loop body
    compiler.genSETA();  // store loop body result in result array
    compiler.genPOP();   // get rid of the result array
    compiler.genJUMP(start); // jump to start of loop

    end.resolve();
    bitterend.resolve();
    compiler.genSWAP();  // flip <input, array> so input on top of stack
    compiler.genPOPI();  // restore input from stack
  }

  public void dump(int level) {
    System.out.println(NodeUtils.indent(level) + "for (");
    valueExpr.dump(level + 1);
    System.out.println(NodeUtils.indent(level) + ")");
    loopExpr.dump(level + 1);
  }
}
