
package com.schibsted.spt.data.jstl2;

import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.impl.*;

public class Parser {
  private static ObjectMapper mapper = new ObjectMapper();

  // this will be replaced with a proper Context. need to figure out
  // relationship between compile-time and run-time context first.
  private static Map<String, Function> functions = new HashMap();
  static {
    functions.put("number", new BuiltinFunctions.Number());
  }

  public static Expression compile(String jstl) {
    try {
      JstlParser parser = new JstlParser(new StringReader(jstl));
      parser.Start();

      // the start production always contains an expr, so we just ditch it
      SimpleNode start = (SimpleNode) parser.jjtree.rootNode();
      SimpleNode expr = (SimpleNode) start.jjtGetChild(0);
      return node2expr(expr);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private static Expression node2expr(SimpleNode node) {
    Token token = node.jjtGetFirstToken();
    if (token.kind == JstlParserConstants.LBRACKET ||
        token.kind == JstlParserConstants.LCURLY ||
        token.kind == JstlParserConstants.IF ||
        token.kind == JstlParserConstants.IDENT /* function call */)
      // it's not a token but a production, so we ditch the Expr node
      // and go down to the level below, which is the detail production
      node = (SimpleNode) node.jjtGetChild(0);

    token = node.jjtGetFirstToken();
    int kind = token.kind;

    if (kind == JstlParserConstants.NULL)
      return new LiteralExpression(NullNode.instance);

    else if (kind == JstlParserConstants.INTEGER) {
      IntNode number = new IntNode(Integer.parseInt(token.image));
      return new LiteralExpression(number);

    } else if (kind == JstlParserConstants.DECIMAL) {
      DoubleNode number = new DoubleNode(Double.parseDouble(token.image));
      return new LiteralExpression(number);

    } else if (kind == JstlParserConstants.STRING)
      return new LiteralExpression(new TextNode(makeString(token)));

    else if (kind == JstlParserConstants.TRUE)
      return new LiteralExpression(BooleanNode.TRUE);

    else if (kind == JstlParserConstants.FALSE)
      return new LiteralExpression(BooleanNode.FALSE);

    else if (kind == JstlParserConstants.DOT) {
      Token last = node.jjtGetLastToken();
      if (last.kind == JstlParserConstants.IDENT)
        return new DotExpression(last.image);
      else
        return new DotExpression();

    } else if (kind == JstlParserConstants.IF) {
      // 2 children: if ($1) $2
      // 3 children: if ($1) $2 else $3
      Expression theelse = null;
      if (node.jjtGetNumChildren() == 3)
        theelse = node2expr((SimpleNode) node.jjtGetChild(2));

      return new IfExpression(
        node2expr((SimpleNode) node.jjtGetChild(0)),
        node2expr((SimpleNode) node.jjtGetChild(1)),
        theelse
      );

    } else if (kind == JstlParserConstants.IDENT) {
      // function call, where the children are the parameters
      Function func = functions.get(token.image);
      if (func == null)
        throw new JstlException("No such function: '" + token.image + "'");
      return new FunctionExpression(func, children2Exprs(node));

    } else if (kind == JstlParserConstants.LBRACKET)
      return new ArrayExpression(mapper, children2Exprs(node));

    else if (kind == JstlParserConstants.LCURLY) {
      PairExpression[] children = new PairExpression[node.jjtGetNumChildren()];
      for (int ix = 0; ix < node.jjtGetNumChildren(); ix++) {
        SimpleNode pair = (SimpleNode) node.jjtGetChild(ix);
        String key = makeString(pair.jjtGetFirstToken());
        Expression val = node2expr((SimpleNode) pair.jjtGetChild(0));
        children[ix] = new PairExpression(key, val);
      }
      return new ObjectExpression(mapper, children);

    } else
      throw new RuntimeException("I'm confused now: " + node.jjtGetNumChildren());
  }

  private static String makeString(Token literal) {
    return literal.image.substring(1, literal.image.length() - 1);
  }

  private static Expression[] children2Exprs(SimpleNode node) {
    Expression[] children = new Expression[node.jjtGetNumChildren()];
    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      children[ix] = node2expr((SimpleNode) node.jjtGetChild(ix));
    return children;
  }
}
