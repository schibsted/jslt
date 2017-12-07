
package com.schibsted.spt.data.jstl2;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.FileNotFoundException;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jstl2.impl.*;
import com.schibsted.spt.data.jstl2.impl.vm.Compiler;

public class Parser {

  public static Expression compile(File jstl) {
    // FIXME: character encoding bug
    try {
      return compile(new JstlParser(new FileReader(jstl)));
    } catch (FileNotFoundException e) {
      throw new JstlException("Couldn't find file " + jstl);
    }
  }

  public static Expression compile(String jstl) {
    return compile(new JstlParser(new StringReader(jstl)));
  }

  private static Expression compile(JstlParser parser) {
    try {
      parser.Start();

      // the start production always contains an expr, so we just ditch it
      SimpleNode start = (SimpleNode) parser.jjtree.rootNode();
      //start.dump(">");

      LetExpression[] lets = buildLets(start);
      SimpleNode expr = getLastChild(start);
      ExpressionNode top = node2expr(expr);
      top = top.optimize();
      ExpressionImpl root = new ExpressionImpl(lets, top);
      //Compiler compiler = new Compiler();
      //return compiler.compile(root);
      return root;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private static ExpressionNode node2expr(SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTEXPR)
      throw new RuntimeException("Wrong type of node: " + node);

    ExpressionNode first = node2andexpr(getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2andexpr(getChild(node, 1));
    return new OrOperator(first, second);
  }

  private static ExpressionNode node2andexpr(SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTANDEXPR)
      throw new RuntimeException("Wrong type of node: " + node);

    ExpressionNode first = node2compexpr(getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2andexpr(getChild(node, 1));
    return new AndOperator(first, second);
  }

  private static ExpressionNode node2compexpr(SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTCOMPARATIVEEXPR)
      throw new RuntimeException("Wrong type of node: " + node);

    ExpressionNode first = node2addexpr(getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2addexpr(getChild(node, 2));

    // get the comparator
    Token comp = getChild(node, 1).jjtGetFirstToken();
    if (comp.kind == JstlParserConstants.EQUALS)
      return new EqualsComparison(first, second);
    else if (comp.kind == JstlParserConstants.UNEQUALS)
      return new UnequalsComparison(first, second);
    else if (comp.kind == JstlParserConstants.BIGOREQ)
      return new BiggerOrEqualComparison(first, second);
    else
      throw new RuntimeException("What kind of comparison is this? " + node);
  }

  private static ExpressionNode node2addexpr(SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTADDITIVEEXPR)
      throw new RuntimeException("Wrong type of node: " + node);

    ExpressionNode first = node2baseExpr(getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2addexpr(getChild(node, 2));

    // get the operator
    Token comp = getChild(node, 1).jjtGetFirstToken();
    if (comp.kind == JstlParserConstants.PLUS)
      return new PlusOperator(first, second);
    else
      throw new RuntimeException("What kind of operator is this?");
  }

  private static ExpressionNode node2baseExpr(SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTBASEEXPR)
      throw new RuntimeException("Wrong type of node: " + node);

    Token token = node.jjtGetFirstToken();
    if (token.kind == JstlParserConstants.LBRACKET ||
        token.kind == JstlParserConstants.LCURLY ||
        token.kind == JstlParserConstants.IF)
      // it's not a token but a production, so we ditch the Expr node
      // and go down to the level below, which holds the actual info
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

    else if (kind == JstlParserConstants.DOT ||
             kind == JstlParserConstants.VARIABLE ||
             kind == JstlParserConstants.IDENT)
      return chainable2Expr(getChild(node, 0));

    else if (kind == JstlParserConstants.FOR)
      return buildForExpression(getChild(node, 0));

    else if (kind == JstlParserConstants.IF) {
      LetExpression[] letelse = null;
      ExpressionNode theelse = null;
      SimpleNode maybeelse = getLastChild(node);
      if (maybeelse.jjtGetFirstToken().kind == JstlParserConstants.ELSE) {
        SimpleNode elseexpr = getLastChild(maybeelse);
        theelse = node2expr(elseexpr);
        letelse = buildLets(maybeelse);
      }

      LetExpression[] thenelse = buildLets(node);

      return new IfExpression(
        node2expr((SimpleNode) node.jjtGetChild(0)),
        thenelse,
        node2expr((SimpleNode) node.jjtGetChild(thenelse.length + 1)),
        letelse,
        theelse
      );

    } else if (kind == JstlParserConstants.LBRACKET)
      return new ArrayExpression(children2Exprs(node));

    else if (kind == JstlParserConstants.LCURLY)
      return buildObject(node);

    else if (kind == JstlParserConstants.LPAREN) {
      // we don't need a node for the parentheses - so just build the
      // child as a single node and use that instead
      SimpleNode parens = descendTo(node, JstlParserTreeConstants.JJTPARENTHESIS);
      return node2expr(getChild(parens, 0));
    }

    else {
      node.dump(">");
      throw new RuntimeException("I'm confused now: " + node.jjtGetNumChildren() + " " + kind);
    }
  }

  private static ExpressionNode chainable2Expr(SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTCHAINABLE)
      throw new RuntimeException("Wrong type of node: " + node);

    Token token = node.jjtGetFirstToken();
    int kind = token.kind;

    // need to special-case the first node
    ExpressionNode start;
    if (kind == JstlParserConstants.VARIABLE)
      start = new VariableExpression(token.image.substring(1));

    else if (kind == JstlParserConstants.IDENT) {
      SimpleNode fnode = descendTo(node, JstlParserTreeConstants.JJTFUNCTIONCALL);

      // function call, where the children are the parameters
      Function func = BuiltinFunctions.functions.get(token.image);
      if (func == null)
        throw new JstlException("No such function: '" + token.image + "'");
      start = new FunctionExpression(func, children2Exprs(fnode));

    } else if (kind == JstlParserConstants.DOT) {
      token = token.next;
      if (token.kind != JstlParserConstants.IDENT &&
          token.kind != JstlParserConstants.STRING &&
          token.kind != JstlParserConstants.LBRACKET)
        return new DotExpression(); // there was only a dot

      // ok, there was a key or array slicer
      start = buildChainLink(node, null);
    } else
      throw new RuntimeException("Now I'm *really* confused!");

    // then tack on the rest of the chain, if there is any
    if (node.jjtGetNumChildren() > 0 &&
        getLastChild(node).id == JstlParserTreeConstants.JJTCHAINLINK)
      return buildDotChain(getLastChild(node), start);
    else
      return start;
  }

  private static ExpressionNode buildDotChain(SimpleNode chainLink,
                                              ExpressionNode parent) {
    if (chainLink.id != JstlParserTreeConstants.JJTCHAINLINK)
      throw new RuntimeException("Wrong type of node: " + chainLink);

    ExpressionNode dot = buildChainLink(chainLink, parent);

    // check if there is more, if so, build
    if (chainLink.jjtGetNumChildren() == 2)
      dot = buildDotChain(getChild(chainLink, 1), dot);

    return dot;
  }

  private static ExpressionNode buildChainLink(SimpleNode node,
                                               ExpressionNode parent) {
    Token token = node.jjtGetFirstToken();

    if (token.kind == JstlParserConstants.DOT) {
      // it's a dotkey
      token = token.next; // step to token after DOT

      if (token.kind == JstlParserConstants.LBRACKET)
        return new DotExpression(); // it's .[...]

      String key = identOrString(token);
      return new DotExpression(key, parent);
    } else
      return buildArraySlicer(getChild(node, 0), parent);
  }

  private static ExpressionNode buildArraySlicer(SimpleNode node,
                                                 ExpressionNode parent) {
    boolean colon = false; // slicer or index?
    ExpressionNode left = null;
    SimpleNode first = getChild(node, 0);
    if (first.id != JstlParserTreeConstants.JJTCOLON)
      left = node2expr(first);

    ExpressionNode right = null;
    SimpleNode last = getLastChild(node);
    if (node.jjtGetNumChildren() != 1 &&
        last.id != JstlParserTreeConstants.JJTCOLON)
      right = node2expr(last);

    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      colon = colon || getChild(node, ix).id == JstlParserTreeConstants.JJTCOLON;

    return new ArraySlicer(left, colon, right, parent);
  }

  private static ForExpression buildForExpression(SimpleNode node) {
    ExpressionNode valueExpr = node2expr(getChild(node, 0));
    ExpressionNode loopExpr = node2expr(getChild(node, 1));
    return new ForExpression(valueExpr, loopExpr);
  }

  private static String identOrString(Token token) {
    if (token.kind == JstlParserConstants.STRING)
      return makeString(token);
    else
      return token.image;
  }

  private static String makeString(Token literal) {
    return literal.image.substring(1, literal.image.length() - 1);
  }

  private static ExpressionNode[] children2Exprs(SimpleNode node) {
    ExpressionNode[] children = new ExpressionNode[node.jjtGetNumChildren()];
    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      children[ix] = node2expr((SimpleNode) node.jjtGetChild(ix));
    return children;
  }

  // collects all the 'let' statements as children of this node
  private static LetExpression[] buildLets(SimpleNode parent) {
    // figure out how many lets there are
    int letCount = countChildren(parent, JstlParserTreeConstants.JJTLET);

    // collect the lets
    int pos = 0;
    LetExpression[] lets = new LetExpression[letCount];
    for (int ix = 0; ix < parent.jjtGetNumChildren(); ix++) {
      SimpleNode node = (SimpleNode) parent.jjtGetChild(ix);
      if (node.firstToken.kind != JstlParserConstants.LET)
        continue;

      Token ident = node.jjtGetFirstToken().next;
      SimpleNode expr = (SimpleNode) node.jjtGetChild(0);
      lets[pos++] = new LetExpression(ident.image, node2expr(expr));
    }
    return lets;
  }

  private static ObjectExpression buildObject(SimpleNode node) {
    LetExpression[] lets = buildLets(node);

    SimpleNode last = getLastChild(node);

    MatcherExpression matcher = collectMatcher(last);
    List<PairExpression> pairs = collectPairs(last);
    PairExpression[] children = new PairExpression[pairs.size()];
    children = pairs.toArray(children);
    return new ObjectExpression(lets, children, matcher);
  }

  private static MatcherExpression collectMatcher(SimpleNode node) {
    if (node == null)
      return null;

    SimpleNode last = getLastChild(node);
    if (node.id == JstlParserTreeConstants.JJTPAIR) {
      if (node.jjtGetNumChildren() == 1)
        return null; // last in chain was a pair

      return collectMatcher(last);
    } else if (node.id == JstlParserTreeConstants.JJTMATCHER) {
      List<String> minuses = new ArrayList();
      if (node.jjtGetNumChildren() == 2) // means there was "* - foo : ..."
        collectMinuses(getChild(node, 0), minuses);
      return new MatcherExpression(node2expr(last), minuses);
    } else
      throw new RuntimeException("This is wrong");
  }

  private static void collectMinuses(SimpleNode node, List<String> minuses) {
    Token token = node.jjtGetFirstToken();
    token = token.next; // skip the -

    while (true) {
      minuses.add(identOrString(token));
      token = token.next;
      if (token.kind == JstlParserConstants.COLON)
        break;
      // else: COMMA
      token = token.next;
    }
  }

  private static List<PairExpression> collectPairs(SimpleNode pair) {
    if (pair != null && pair.id == JstlParserTreeConstants.JJTPAIR) {
      String key = makeString(pair.jjtGetFirstToken());
      ExpressionNode val = node2expr((SimpleNode) pair.jjtGetChild(0));

      List<PairExpression> pairs;
      if (pair.jjtGetNumChildren() == 1)
        pairs = new ArrayList(); // no more pairs
      else
        pairs = collectPairs(getLastChild(pair));

      pairs.add(new PairExpression(key, val));
      return pairs;
    } else
      // has to be a matcher, so we're done
      return new ArrayList();
  }

  private static SimpleNode getChild(SimpleNode node, int ix) {
    return (SimpleNode) node.jjtGetChild(ix);
  }

  private static SimpleNode getLastChild(SimpleNode node) {
    if (node.jjtGetNumChildren() == 0)
      return null;
    return (SimpleNode) node.jjtGetChild(node.jjtGetNumChildren() - 1);
  }

  private static SimpleNode descendTo(SimpleNode node, int type) {
    if (node.id == type)
      return node;

    return descendTo((SimpleNode) node.jjtGetChild(0), type);
  }

  private static int countChildren(SimpleNode node, int type) {
    int count = 0;
    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      if (getChild(node, ix).id == type)
        count++;
    return count;
  }
}
