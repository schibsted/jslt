
package com.schibsted.spt.data.jstl2;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    try (FileReader f = new FileReader(jstl)) {
      ParseContext ctx = new ParseContext(jstl.getAbsolutePath());
      return compile(ctx, new JstlParser(f));
    } catch (FileNotFoundException e) {
      throw new JstlException("Couldn't find file " + jstl);
    } catch (IOException e) {
      throw new JstlException("Couldn't read file " + jstl, e);
    }
  }

  public static Expression compile(String jstl) {
    ParseContext ctx = new ParseContext("<unknown>");
    return compile(ctx, new JstlParser(new StringReader(jstl)));
  }

  public static Expression compile(Collection<Function> functions,
                                   String jstl) {
    ParseContext ctx = new ParseContext(functions, null);
    return compile(ctx, new JstlParser(new StringReader(jstl)));
  }

  public static Expression compileResource(String jstl) {
    return compileResource(Collections.EMPTY_SET, jstl);
  }

  /**
   * Compile JSTL2 template from the Reader. The resourceName is just
   * a name used in error messages, and has no practical effect.
   */
  public static Expression compile(Collection<Function> functions,
                                   String resourceName,
                                   Reader reader) {
    ParseContext ctx = new ParseContext(functions, resourceName);
    return compile(ctx, new JstlParser(reader));
  }

  public static Expression compileResource(Collection<Function> functions,
                                           String jstl) {
    try (InputStream stream = Parser.class.getClassLoader().getResourceAsStream(jstl)) {
      if (stream == null)
        throw new JstlException("Cannot load resource '" + jstl + "': not found");

      Reader reader = new InputStreamReader(stream, "UTF-8");
      ParseContext ctx = new ParseContext(functions, jstl);
      return compile(ctx, new JstlParser(reader));
    } catch (IOException e) {
      throw new JstlException("Couldn't read resource " + jstl, e);
    }
  }

  private static Expression compile(ParseContext ctx, JstlParser parser) {
    try {
      parser.Start();

      // the start production always contains an expr, so we just ditch it
      SimpleNode start = (SimpleNode) parser.jjtree.rootNode();
      //start.dump(">");

      LetExpression[] lets = buildLets(ctx, start);
      SimpleNode expr = getLastChild(start);
      ExpressionNode top = node2expr(ctx, expr);
      top = top.optimize();
      ExpressionImpl root = new ExpressionImpl(lets, top);
      //Compiler compiler = new Compiler();
      //return compiler.compile(root);
      return root;
    } catch (ParseException e) {
      throw new JstlException("Parse error: " + e.getMessage(),
                              makeLocation(ctx, e.currentToken));
    }
  }

  private static ExpressionNode node2expr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTEXPR)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2andexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2expr(ctx, getChild(node, 1));
    return new OrOperator(first, second, makeLocation(ctx, node));
  }

  private static ExpressionNode node2andexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTANDEXPR)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2compexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2andexpr(ctx, getChild(node, 1));
    return new AndOperator(first, second, makeLocation(ctx, node));
  }

  private static ExpressionNode node2compexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTCOMPARATIVEEXPR)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2addexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2addexpr(ctx, getChild(node, 2));

    // get the comparator
    Location loc = makeLocation(ctx, node);
    Token comp = getChild(node, 1).jjtGetFirstToken();
    if (comp.kind == JstlParserConstants.EQUALS)
      return new EqualsComparison(first, second, loc);
    else if (comp.kind == JstlParserConstants.UNEQUALS)
      return new UnequalsComparison(first, second, loc);
    else if (comp.kind == JstlParserConstants.BIGOREQ)
      return new BiggerOrEqualComparison(first, second, loc);
    else if (comp.kind == JstlParserConstants.BIGGER)
      return new BiggerComparison(first, second, loc);
    else if (comp.kind == JstlParserConstants.SMALLER)
      return new SmallerComparison(first, second, loc);
    else if (comp.kind == JstlParserConstants.SMALLOREQ)
      return new SmallerOrEqualsComparison(first, second, loc);
    else
      throw new JstlException("INTERNAL ERROR: What kind of comparison is this? " + node);
  }

  private static ExpressionNode node2addexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTADDITIVEEXPR)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2mulexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2addexpr(ctx, getChild(node, 2));

    // get the operator
    Location loc = makeLocation(ctx, node);
    Token comp = getChild(node, 1).jjtGetFirstToken();
    if (comp.kind == JstlParserConstants.PLUS)
      return new PlusOperator(first, second, loc);
    else if (comp.kind == JstlParserConstants.MINUS)
      return new MinusOperator(first, second, loc);
    else
      throw new JstlException("INTERNAL ERROR: What kind of operator is this?");
  }

  private static ExpressionNode node2mulexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTMULTIPLICATIVEEXPR)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2baseExpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2mulexpr(ctx, getChild(node, 2));

    // get the operator
    Location loc = makeLocation(ctx, node);
    Token comp = getChild(node, 1).jjtGetFirstToken();
    if (comp.kind == JstlParserConstants.STAR)
      return new MultiplyOperator(first, second, loc);
    else if (comp.kind == JstlParserConstants.SLASH)
      return new DivideOperator(first, second, loc);
    else
      throw new JstlException("INTERNAL ERROR: What kind of operator is this?");
  }

  private static ExpressionNode node2baseExpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTBASEEXPR)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    Location loc = makeLocation(ctx, node);
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
      return new LiteralExpression(NullNode.instance, loc);

    else if (kind == JstlParserConstants.INTEGER) {
      IntNode number = new IntNode(Integer.parseInt(token.image));
      return new LiteralExpression(number, loc);

    } else if (kind == JstlParserConstants.DECIMAL) {
      DoubleNode number = new DoubleNode(Double.parseDouble(token.image));
      return new LiteralExpression(number, loc);

    } else if (kind == JstlParserConstants.STRING)
      return new LiteralExpression(new TextNode(makeString(ctx, token)), loc);

    else if (kind == JstlParserConstants.TRUE)
      return new LiteralExpression(BooleanNode.TRUE, loc);

    else if (kind == JstlParserConstants.FALSE)
      return new LiteralExpression(BooleanNode.FALSE, loc);

    else if (kind == JstlParserConstants.DOT ||
             kind == JstlParserConstants.VARIABLE ||
             kind == JstlParserConstants.IDENT)
      return chainable2Expr(ctx, getChild(node, 0));

    else if (kind == JstlParserConstants.FOR)
      return buildForExpression(ctx, getChild(node, 0));

    else if (kind == JstlParserConstants.IF) {
      LetExpression[] letelse = null;
      ExpressionNode theelse = null;
      SimpleNode maybeelse = getLastChild(node);
      if (maybeelse.jjtGetFirstToken().kind == JstlParserConstants.ELSE) {
        SimpleNode elseexpr = getLastChild(maybeelse);
        theelse = node2expr(ctx, elseexpr);
        letelse = buildLets(ctx, maybeelse);
      }

      LetExpression[] thenelse = buildLets(ctx, node);

      return new IfExpression(
        node2expr(ctx, (SimpleNode) node.jjtGetChild(0)),
        thenelse,
        node2expr(ctx, (SimpleNode) node.jjtGetChild(thenelse.length + 1)),
        letelse,
        theelse,
        loc
      );

    } else if (kind == JstlParserConstants.LBRACKET)
      return new ArrayExpression(children2Exprs(ctx, node), loc);

    else if (kind == JstlParserConstants.LCURLY) {
      Token next = token.next;
      if (next.kind == JstlParserConstants.FOR)
        return buildObjectComprehension(ctx, node);
      else
        return buildObject(ctx, node);

    } else if (kind == JstlParserConstants.LPAREN) {
      // we don't need a node for the parentheses - so just build the
      // child as a single node and use that instead
      SimpleNode parens = descendTo(node, JstlParserTreeConstants.JJTPARENTHESIS);
      return node2expr(ctx, getChild(parens, 0));
    }

    else {
      node.dump(">");
      throw new JstlException("INTERNAL ERROR: I'm confused now: " +
                              node.jjtGetNumChildren() + " " + kind);
    }
  }

  private static ExpressionNode chainable2Expr(ParseContext ctx, SimpleNode node) {
    if (node.id != JstlParserTreeConstants.JJTCHAINABLE)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + node);

    Token token = node.jjtGetFirstToken();
    int kind = token.kind;

    Location loc = makeLocation(ctx, node);

    // need to special-case the first node
    ExpressionNode start;
    if (kind == JstlParserConstants.VARIABLE)
      start = new VariableExpression(token.image.substring(1), loc);

    else if (kind == JstlParserConstants.IDENT) {
      SimpleNode fnode = descendTo(node, JstlParserTreeConstants.JJTFUNCTIONCALL);

      // function call, where the children are the parameters
      Function func = ctx.getFunction(token.image);
      if (func == null) {
        // it could still be a macro
        Macro mac = ctx.getMacro(token.image);
        if (mac == null)
          throw new JstlException("No such function: '" + token.image + "'", loc);
        start = new MacroExpression(mac, children2Exprs(ctx, fnode), loc);
      } else
        start = new FunctionExpression(func, children2Exprs(ctx, fnode), loc);

    } else if (kind == JstlParserConstants.DOT) {
      token = token.next;
      if (token.kind != JstlParserConstants.IDENT &&
          token.kind != JstlParserConstants.STRING &&
          token.kind != JstlParserConstants.LBRACKET)
        return new DotExpression(loc); // there was only a dot

      // ok, there was a key or array slicer
      start = buildChainLink(ctx, node, null);
    } else
      throw new JstlException("INTERNAL ERROR: Now I'm *really* confused!");

    // then tack on the rest of the chain, if there is any
    if (node.jjtGetNumChildren() > 0 &&
        getLastChild(node).id == JstlParserTreeConstants.JJTCHAINLINK)
      return buildDotChain(ctx, getLastChild(node), start);
    else
      return start;
  }

  private static ExpressionNode buildDotChain(ParseContext ctx,
                                              SimpleNode chainLink,
                                              ExpressionNode parent) {
    if (chainLink.id != JstlParserTreeConstants.JJTCHAINLINK)
      throw new JstlException("INTERNAL ERROR: Wrong type of node: " + chainLink);

    ExpressionNode dot = buildChainLink(ctx, chainLink, parent);

    // check if there is more, if so, build
    if (chainLink.jjtGetNumChildren() == 2)
      dot = buildDotChain(ctx, getChild(chainLink, 1), dot);

    return dot;
  }

  private static ExpressionNode buildChainLink(ParseContext ctx,
                                               SimpleNode node,
                                               ExpressionNode parent) {
    Token token = node.jjtGetFirstToken();

    if (token.kind == JstlParserConstants.DOT) {
      // it's a dotkey
      token = token.next; // step to token after DOT

      Location loc = makeLocation(ctx, node);
      if (token.kind == JstlParserConstants.LBRACKET)
        return new DotExpression(loc); // it's .[...]

      String key = identOrString(ctx, token);
      return new DotExpression(key, parent, loc);
    } else
      return buildArraySlicer(ctx, getChild(node, 0), parent);
  }

  private static ExpressionNode buildArraySlicer(ParseContext ctx,
                                                 SimpleNode node,
                                                 ExpressionNode parent) {
    boolean colon = false; // slicer or index?
    ExpressionNode left = null;
    SimpleNode first = getChild(node, 0);
    if (first.id != JstlParserTreeConstants.JJTCOLON)
      left = node2expr(ctx, first);

    ExpressionNode right = null;
    SimpleNode last = getLastChild(node);
    if (node.jjtGetNumChildren() != 1 &&
        last.id != JstlParserTreeConstants.JJTCOLON)
      right = node2expr(ctx, last);

    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      colon = colon || getChild(node, ix).id == JstlParserTreeConstants.JJTCOLON;

    Location loc = makeLocation(ctx, node);
    return new ArraySlicer(left, colon, right, parent, loc);
  }

  private static ForExpression buildForExpression(ParseContext ctx, SimpleNode node) {
    ExpressionNode valueExpr = node2expr(ctx, getChild(node, 0));
    ExpressionNode loopExpr = node2expr(ctx, getChild(node, 1));
    return new ForExpression(valueExpr, loopExpr, makeLocation(ctx, node));
  }

  private static String identOrString(ParseContext ctx, Token token) {
    if (token.kind == JstlParserConstants.STRING)
      return makeString(ctx, token);
    else
      return token.image;
  }

  private static String makeString(ParseContext ctx, Token literal) {
    // we need to handle escape sequences, so therefore we walk
    // through the entire string, building the output step by step
    String string = literal.image;
    char[] result = new char[string.length() - 2];
    int pos = 0; // position in result array
    for (int ix = 1; ix < string.length() - 1; ix++) {
      char ch = string.charAt(ix);
      if (ch != '\\')
        result[pos++] = ch;
      else {
        ch = string.charAt(++ix);

        switch (ch) {
        case '\\': result[pos++] = ch; break;
        case '"': result[pos++] = ch; break;
        case 'n': result[pos++] = '\n'; break;
        case 'b': result[pos++] = '\u0008'; break;
        case 'f': result[pos++] = '\f'; break;
        case 'r': result[pos++] = '\r'; break;
        case 't': result[pos++] = '\t'; break;
        case '/': result[pos++] = '/'; break;
        case 'u':
          if (ix + 5 >= string.length())
            throw new JstlException("Unfinished Unicode escape sequence",
                                    makeLocation(ctx, literal));
          result[pos++] = interpretUnicodeEscape(string, ix + 1);
          ix += 4;
          break;
        default: throw new JstlException("Unknown escape sequence: \\" + ch,
                                         makeLocation(ctx, literal));
        }
      }
    }
    return new String(result, 0, pos);
  }

  private static char interpretUnicodeEscape(String string, int start) {
    int codepoint = 0;
    for (int ix = 0; ix < 4; ix++)
      codepoint = codepoint * 16 + interpretHexDigit(string.charAt(start + ix));
    return (char) codepoint;
  }

  private static char interpretHexDigit(char digit) {
    if (digit >= '0' && digit <= '9')
      return (char) (digit - '0');
    else if (digit >= 'A' && digit <= 'F')
      return (char) ((digit - 'A') + 10);
    else if (digit >= 'a' && digit <= 'f')
      return (char) ((digit - 'a') + 10);

    throw new JstlException("Bad Unicode escape hex digit: '" + digit + "'");
  }

  private static ExpressionNode[] children2Exprs(ParseContext ctx,
                                                 SimpleNode node) {
    ExpressionNode[] children = new ExpressionNode[node.jjtGetNumChildren()];
    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      children[ix] = node2expr(ctx, (SimpleNode) node.jjtGetChild(ix));
    return children;
  }

  // collects all the 'let' statements as children of this node
  private static LetExpression[] buildLets(ParseContext ctx, SimpleNode parent) {
    // figure out how many lets there are
    int letCount = countChildren(parent, JstlParserTreeConstants.JJTLET);

    // collect the lets
    int pos = 0;
    LetExpression[] lets = new LetExpression[letCount];
    for (int ix = 0; ix < parent.jjtGetNumChildren(); ix++) {
      SimpleNode node = (SimpleNode) parent.jjtGetChild(ix);
      if (node.firstToken.kind != JstlParserConstants.LET)
        continue;

      Location loc = makeLocation(ctx, node);
      Token ident = node.jjtGetFirstToken().next;
      SimpleNode expr = (SimpleNode) node.jjtGetChild(0);
      lets[pos++] = new LetExpression(ident.image, node2expr(ctx, expr), loc);
    }
    return lets;
  }

  private static ObjectExpression buildObject(ParseContext ctx, SimpleNode node) {
    LetExpression[] lets = buildLets(ctx, node);

    SimpleNode last = getLastChild(node);

    MatcherExpression matcher = collectMatcher(ctx, last);
    List<PairExpression> pairs = collectPairs(ctx, last);
    PairExpression[] children = new PairExpression[pairs.size()];
    children = pairs.toArray(children);
    return new ObjectExpression(lets, children, matcher,
                                makeLocation(ctx, node));
  }

  private static MatcherExpression collectMatcher(ParseContext ctx,
                                                  SimpleNode node) {
    if (node == null)
      return null;

    SimpleNode last = getLastChild(node);
    if (node.id == JstlParserTreeConstants.JJTPAIR) {
      if (node.jjtGetNumChildren() == 1)
        return null; // last in chain was a pair

      return collectMatcher(ctx, last);
    } else if (node.id == JstlParserTreeConstants.JJTMATCHER) {
      List<String> minuses = new ArrayList();
      if (node.jjtGetNumChildren() == 2) // means there was "* - foo : ..."
        collectMinuses(ctx, getChild(node, 0), minuses);
      return new MatcherExpression(node2expr(ctx, last), minuses,
                                   makeLocation(ctx, last));
    } else
      throw new JstlException("INTERNAL ERROR: This is wrong");
  }

  private static void collectMinuses(ParseContext ctx, SimpleNode node,
                                     List<String> minuses) {
    Token token = node.jjtGetFirstToken();
    token = token.next; // skip the -

    while (true) {
      minuses.add(identOrString(ctx, token));
      token = token.next;
      if (token.kind == JstlParserConstants.COLON)
        break;
      // else: COMMA
      token = token.next;
    }
  }

  private static List<PairExpression> collectPairs(ParseContext ctx,
                                                   SimpleNode pair) {
    if (pair != null && pair.id == JstlParserTreeConstants.JJTPAIR) {
      String key = makeString(ctx, pair.jjtGetFirstToken());
      ExpressionNode val = node2expr(ctx, (SimpleNode) pair.jjtGetChild(0));

      List<PairExpression> pairs;
      if (pair.jjtGetNumChildren() == 1)
        pairs = new ArrayList(); // no more pairs
      else
        pairs = collectPairs(ctx, getLastChild(pair));

      pairs.add(new PairExpression(key, val, makeLocation(ctx, pair)));
      return pairs;
    } else
      // has to be a matcher, so we're done
      return new ArrayList();
  }

  private static ObjectComprehension buildObjectComprehension(ParseContext ctx, SimpleNode node) {
    // children: loop-expr let* key-expr value-expr
    ExpressionNode loopExpr = node2expr(ctx, getChild(node, 0));
    int ix = node.jjtGetNumChildren() - 2;
    ExpressionNode keyExpr = node2expr(ctx, getChild(node, ix));
    ExpressionNode valueExpr = node2expr(ctx, getLastChild(node));
    return new ObjectComprehension(loopExpr, keyExpr, valueExpr,
                                   makeLocation(ctx, node));
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

  private static Location makeLocation(ParseContext ctx, SimpleNode node) {
    Token token = node.jjtGetFirstToken();
    return new Location(ctx.getSource(), token.beginLine, token.beginColumn);
  }

  private static Location makeLocation(ParseContext ctx, Token token) {
    return new Location(ctx.getSource(), token.beginLine, token.beginColumn);
  }
}
