
// Copyright 2018 Schibsted Marketplaces Products & Technology As
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.schibsted.spt.data.jslt.parser;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.schibsted.spt.data.jslt.Module;
import com.schibsted.spt.data.jslt.Callable;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.impl.*;
import com.schibsted.spt.data.jslt.filters.JsonFilter;

public class ParserImpl {

  public static Expression compileExpression(ParseContext ctx, JsltParser parser) {
    try {
      parser.Start();
      //((SimpleNode) parser.jjtree.rootNode()).dump("");
      ExpressionImpl expr = compile(ctx, (SimpleNode) parser.jjtree.rootNode());
      expr.setGlobalModules(ctx.getFiles());
      return expr;

    } catch (ParseException e) {
      throw new JsltException("Parse error: " + e.getMessage(),
                              makeLocation(ctx, e.currentToken));
    } catch (TokenMgrError e) {
      throw new JsltException("Parse error: " + e.getMessage());
    }
  }

  private static ExpressionImpl compileImport(Collection<Function> functions,
                                             ParseContext parent,
                                             String jslt) {
    try (Reader reader = parent.getResolver().resolve(jslt)) {
      ParseContext ctx = new ParseContext(functions, jslt, parent.getResolver(), parent.getNamedModules(), parent.getFiles(), parent.getPreparationContext(), parent.getObjectFilter());
      ctx.setParent(parent);
      return compileModule(ctx, new JsltParser(reader));
    } catch (IOException e) {
      throw new JsltException("Couldn't read resource " + jslt, e);
    }
  }

  private static ExpressionImpl compileModule(ParseContext ctx, JsltParser parser) {
    try {
      parser.Module();
      return compile(ctx, (SimpleNode) parser.jjtree.rootNode());

    } catch (ParseException e) {
      throw new JsltException("Parse error: " + e.getMessage(),
                              makeLocation(ctx, e.currentToken));
    } catch (TokenMgrError e) {
      throw new JsltException("Parse error: " + e.getMessage());
    }
  }

  private static ExpressionImpl compile(ParseContext ctx, SimpleNode root) {
    processImports(ctx, root); // registered with context
    LetExpression[] lets = buildLets(ctx, root);
    collectFunctions(ctx, root); // registered with context

    SimpleNode expr = getLastChild(root);

    ExpressionNode top = null;
    if (expr.id == JsltParserTreeConstants.JJTEXPR)
      top = node2expr(ctx, expr);
    ctx.resolveFunctions();

    ExpressionImpl impl =
      new ExpressionImpl(lets, ctx.getDeclaredFunctions(), top);
    impl.prepare(ctx.getPreparationContext());
    impl.optimize();
    return impl;
  }

  private static ExpressionNode node2expr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTEXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode root = node2orexpr(ctx, getChild(node, 0));

    int ix = 0;
    while (node.jjtGetNumChildren() > ix * 2 + 1) {
      final SimpleNode child1 = getChild(node, 2 + ix * 2);
      ExpressionNode next = node2orexpr(ctx, child1);

      // get the operator
      Location loc = makeLocation(ctx, node);
      final SimpleNode child2 = getChild(node, 1 + ix * 2);
      Token comp = child2.jjtGetFirstToken();
      if (comp.kind == JsltParserConstants.PIPE)
        root = new PipeOperator(root, next, loc);
      else
        throw new JsltException("INTERNAL ERROR: What kind of operator is this?");
      ix += 1;
    }

    return root;
  }

  private static ExpressionNode node2orexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTOREXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2andexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2orexpr(ctx, getChild(node, 1));
    return new OrOperator(first, second, makeLocation(ctx, node));
  }


  private static ExpressionNode node2andexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTANDEXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2compexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2andexpr(ctx, getChild(node, 1));
    return new AndOperator(first, second, makeLocation(ctx, node));
  }

  private static ExpressionNode node2compexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTCOMPARATIVEEXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode first = node2addexpr(ctx, getChild(node, 0));
    if (node.jjtGetNumChildren() == 1) // it's just the base
      return first;

    ExpressionNode second = node2addexpr(ctx, getChild(node, 2));

    // get the comparator
    Location loc = makeLocation(ctx, node);
    Token comp = getChild(node, 1).jjtGetFirstToken();
    if (comp.kind == JsltParserConstants.EQUALS)
      return new EqualsComparison(first, second, loc);
    else if (comp.kind == JsltParserConstants.UNEQUALS)
      return new UnequalsComparison(first, second, loc);
    else if (comp.kind == JsltParserConstants.BIGOREQ)
      return new BiggerOrEqualComparison(first, second, loc);
    else if (comp.kind == JsltParserConstants.BIGGER)
      return new BiggerComparison(first, second, loc);
    else if (comp.kind == JsltParserConstants.SMALLER)
      return new SmallerComparison(first, second, loc);
    else if (comp.kind == JsltParserConstants.SMALLOREQ)
      return new SmallerOrEqualsComparison(first, second, loc);
    else
      throw new JsltException("INTERNAL ERROR: What kind of comparison is this? " + node);
  }

  private static ExpressionNode node2addexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTADDITIVEEXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode root = node2mulexpr(ctx, getChild(node, 0));

    int ix = 0;
    while (node.jjtGetNumChildren() > ix * 2 + 1) {
      ExpressionNode next = node2mulexpr(ctx, getChild(node, 2 + ix * 2));

      // get the operator
      Location loc = makeLocation(ctx, node);
      Token comp = getChild(node, 1 + ix * 2).jjtGetFirstToken();
      if (comp.kind == JsltParserConstants.PLUS)
        root = new PlusOperator(root, next, loc);
      else if (comp.kind == JsltParserConstants.MINUS)
        root = new MinusOperator(root, next, loc);
      else
        throw new JsltException("INTERNAL ERROR: What kind of operator is this?");

      ix += 1;
    }

    return root;
  }

  private static ExpressionNode node2mulexpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTMULTIPLICATIVEEXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    ExpressionNode root = node2baseExpr(ctx, getChild(node, 0));

    int ix = 0;
    while (node.jjtGetNumChildren() > ix * 2 + 1) {
      final SimpleNode child1 = getChild(node, 2 + ix * 2);
      ExpressionNode next = node2baseExpr(ctx, child1);

      // get the operator
      Location loc = makeLocation(ctx, node);
      final SimpleNode child2 = getChild(node, 1 + ix * 2);
      Token comp = child2.jjtGetFirstToken();
      if (comp.kind == JsltParserConstants.STAR)
        root = new MultiplyOperator(root, next, loc);
      else if (comp.kind == JsltParserConstants.SLASH)
        root = new DivideOperator(root, next, loc);
      else
        throw new JsltException("INTERNAL ERROR: What kind of operator is this?");
      ix += 1;
    }

    return root;
  }

  private static ExpressionNode node2baseExpr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTBASEEXPR)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    Location loc = makeLocation(ctx, node);
    Token token = node.jjtGetFirstToken();
    if (token.kind == JsltParserConstants.LBRACKET ||
        token.kind == JsltParserConstants.LCURLY ||
        token.kind == JsltParserConstants.IF)
      // it's not a token but a production, so we ditch the Expr node
      // and go down to the level below, which holds the actual info
      node = (SimpleNode) node.jjtGetChild(0);

    token = node.jjtGetFirstToken();
    int kind = token.kind;

    if (kind == JsltParserConstants.NULL)
      return new LiteralExpression(NullNode.instance, loc);

    else if (kind == JsltParserConstants.INTEGER) {
      JsonNode numberObj;
      long number = Long.parseLong(token.image);
      if (number > Integer.MAX_VALUE || number < Integer.MIN_VALUE)
        numberObj = new LongNode(number);
      else
        numberObj = new IntNode((int) number);

      return new LiteralExpression(numberObj, loc);

    } else if (kind == JsltParserConstants.DECIMAL) {
      DoubleNode number = new DoubleNode(Double.parseDouble(token.image));
      return new LiteralExpression(number, loc);

    } else if (kind == JsltParserConstants.STRING)
      return new LiteralExpression(new TextNode(makeString(ctx, token)), loc);

    else if (kind == JsltParserConstants.TRUE)
      return new LiteralExpression(BooleanNode.TRUE, loc);

    else if (kind == JsltParserConstants.FALSE)
      return new LiteralExpression(BooleanNode.FALSE, loc);

    else if (kind == JsltParserConstants.DOT ||
             kind == JsltParserConstants.VARIABLE ||
             kind == JsltParserConstants.IDENT ||
             kind == JsltParserConstants.PIDENT)
      return chainable2Expr(ctx, getChild(node, 0));

    else if (kind == JsltParserConstants.IF) {
      LetExpression[] letelse = null;
      ExpressionNode theelse = null;
      SimpleNode maybeelse = getLastChild(node);
      if (maybeelse.jjtGetFirstToken().kind == JsltParserConstants.ELSE) {
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

    } else if (kind == JsltParserConstants.LBRACKET) {
      Token next = token.next;
      if (next.kind == JsltParserConstants.FOR)
        return buildForExpression(ctx, node);
      else
        return new ArrayExpression(children2Exprs(ctx, node), loc);

    } else if (kind == JsltParserConstants.LCURLY) {
      Token next = token.next;
      if (next.kind == JsltParserConstants.FOR)
        return buildObjectComprehension(ctx, node);
      else
        return buildObject(ctx, node);

    } else if (kind == JsltParserConstants.LPAREN) {
      // we don't need a node for the parentheses - so just build the
      // child as a single node and use that instead
      SimpleNode parens = descendTo(node, JsltParserTreeConstants.JJTPARENTHESIS);
      return node2expr(ctx, getChild(parens, 0));
    }

    else {
      node.dump(">");
      throw new JsltException("INTERNAL ERROR: I'm confused now: " +
                              node.jjtGetNumChildren() + " " + kind);
    }
  }

  private static ExpressionNode chainable2Expr(ParseContext ctx, SimpleNode node) {
    if (node.id != JsltParserTreeConstants.JJTCHAINABLE)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + node);

    Token token = node.jjtGetFirstToken();
    int kind = token.kind;

    Location loc = makeLocation(ctx, node);

    // need to special-case the first node
    ExpressionNode start;
    if (kind == JsltParserConstants.VARIABLE)
      start = new VariableExpression(token.image.substring(1), loc);

    else if (kind == JsltParserConstants.IDENT) {
      SimpleNode fnode = descendTo(node, JsltParserTreeConstants.JJTFUNCTIONCALL);

      // function or macro call, where the children are the parameters
      Macro mac = ctx.getMacro(token.image);
      if (mac != null)
        start = new MacroExpression(mac, children2Exprs(ctx, fnode), loc);
      else {
        // we don't resolve the function here, because it may not have been
        // declared yet. instead we store the name, and do the resolution
        // later
        start = new FunctionExpression(
          token.image, children2Exprs(ctx, fnode), loc
        );
        // remember, so we can resolve later
        ctx.rememberFunctionCall((FunctionExpression) start);
      }

    } else if (kind == JsltParserConstants.PIDENT) {
      SimpleNode fnode = descendTo(node, JsltParserTreeConstants.JJTFUNCTIONCALL);

      // imported function must already be there and cannot be a macro
      String pident = token.image;
      int colon = pident.indexOf(':'); // grammar ensures it's there
      String prefix = pident.substring(0, colon);
      String name = pident.substring(colon + 1);

      // throws exception if something fails
      Callable c = ctx.getImportedCallable(prefix, name, loc);

      if (c instanceof Function) {
        FunctionExpression fun = new FunctionExpression(
          pident, children2Exprs(ctx, fnode), loc
        );
        fun.resolve((Function) c);
        start = fun;
      } else
        start = new MacroExpression((Macro) c, children2Exprs(ctx, fnode), loc);

    } else if (kind == JsltParserConstants.DOT) {
      token = token.next;
      if (token.kind != JsltParserConstants.IDENT &&
          token.kind != JsltParserConstants.STRING &&
          token.kind != JsltParserConstants.LBRACKET)
        return new DotExpression(loc); // there was only a dot

      // ok, there was a key or array slicer
      start = buildChainLink(ctx, node, null);
    } else
      throw new JsltException("INTERNAL ERROR: Now I'm *really* confused!");

    // then tack on the rest of the chain, if there is any
    if (node.jjtGetNumChildren() > 0 &&
        getLastChild(node).id == JsltParserTreeConstants.JJTCHAINLINK)
      return buildDotChain(ctx, getLastChild(node), start);
    else
      return start;
  }

  private static ExpressionNode buildDotChain(ParseContext ctx,
                                              SimpleNode chainLink,
                                              ExpressionNode parent) {
    if (chainLink.id != JsltParserTreeConstants.JJTCHAINLINK)
      throw new JsltException("INTERNAL ERROR: Wrong type of node: " + chainLink);

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

    if (token.kind == JsltParserConstants.DOT) {
      // it's a dotkey
      token = token.next; // step to token after DOT

      Location loc = makeLocation(ctx, node);
      if (token.kind == JsltParserConstants.LBRACKET)
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
    if (first.id != JsltParserTreeConstants.JJTCOLON)
      left = node2expr(ctx, first);

    ExpressionNode right = null;
    SimpleNode last = getLastChild(node);
    if (node.jjtGetNumChildren() != 1 &&
        last.id != JsltParserTreeConstants.JJTCOLON)
      right = node2expr(ctx, last);

    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      colon = colon || getChild(node, ix).id == JsltParserTreeConstants.JJTCOLON;

    Location loc = makeLocation(ctx, node);
    return new ArraySlicer(left, colon, right, parent, loc);
  }

  private static ForExpression buildForExpression(ParseContext ctx, SimpleNode node) {
    ExpressionNode valueExpr = node2expr(ctx, getChild(node, 0));
    LetExpression[] lets = buildLets(ctx, node);
    ExpressionNode loopExpr = node2expr(ctx, getLastChild(node));

    ExpressionNode ifExpr = null;
    if (node.jjtGetNumChildren() > 2 + lets.length) {
      // there is an if expression, so what we thought was the loopExpr
      // was actually the ifExpr
      ifExpr = loopExpr;
      // now get the correct loopExpr
      loopExpr = node2expr(ctx, (SimpleNode) getChild(node, node.jjtGetNumChildren() - 2));
    }

    return new ForExpression(valueExpr, lets, loopExpr, ifExpr, makeLocation(ctx, node));
  }

  private static String identOrString(ParseContext ctx, Token token) {
    if (token.kind == JsltParserConstants.STRING)
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
            throw new JsltException("Unfinished Unicode escape sequence",
                                    makeLocation(ctx, literal));
          result[pos++] = interpretUnicodeEscape(string, ix + 1);
          ix += 4;
          break;
        default: throw new JsltException("Unknown escape sequence: \\" + ch,
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

    throw new JsltException("Bad Unicode escape hex digit: '" + digit + "'");
  }

  private static ExpressionNode[] children2Exprs(ParseContext ctx,
                                                 SimpleNode node) {
    ExpressionNode[] children = new ExpressionNode[node.jjtGetNumChildren()];
    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
      children[ix] = node2expr(ctx, (SimpleNode) node.jjtGetChild(ix));
    return children;
  }

  // performs all the import directives and register the prefixes
  private static void processImports(ParseContext ctx, SimpleNode parent) {
    for (int ix = 0; ix < parent.jjtGetNumChildren(); ix++) {
      SimpleNode node = (SimpleNode) parent.jjtGetChild(ix);
      if (node.firstToken.kind != JsltParserConstants.IMPORT)
        continue;

      Token token = node.jjtGetFirstToken(); // 'import'
      token = token.next; // source
      String source = makeString(ctx, token);
      token = token.next; // 'as'
      token = token.next; // prefix
      String prefix = token.image;

      // first check if it's a named module
      Module module = ctx.getNamedModule(source);
      if (module != null)
        ctx.registerModule(prefix, module);
      else {
        // it's not, so load
        JstlFile file = doImport(ctx, source, node, prefix);
        ctx.registerModule(prefix, file);
        ctx.addDeclaredFunction(prefix, file);
        ctx.registerJsltFile(file);
      }
    }
  }

  private static JstlFile doImport(ParseContext parent, String source,
                                   SimpleNode node, String prefix) {
    if (parent.isAlreadyImported(source))
      throw new JsltException("Module '" + source + "' is already imported",
                              makeLocation(parent, node));

    ExpressionImpl expr = compileImport(parent.getExtensions(), parent, source);
    return new JstlFile(prefix, source, expr);
  }

  // collects all the 'let' statements as children of this node
  private static LetExpression[] buildLets(ParseContext ctx, SimpleNode parent) {
    // figure out how many lets there are
    int letCount = countChildren(parent, JsltParserTreeConstants.JJTLET);

    // collect the lets
    int pos = 0;
    LetExpression[] lets = new LetExpression[letCount];
    for (int ix = 0; ix < parent.jjtGetNumChildren(); ix++) {
      SimpleNode node = (SimpleNode) parent.jjtGetChild(ix);
      if (node.firstToken.kind != JsltParserConstants.LET)
        continue;

      Location loc = makeLocation(ctx, node);
      Token ident = node.jjtGetFirstToken().next;
      SimpleNode expr = (SimpleNode) node.jjtGetChild(0);
      lets[pos++] = new LetExpression(ident.image, node2expr(ctx, expr), loc);
    }
    return lets;
  }

  // collects all the 'def' statements as children of this node
  // functions are registered with the context
  private static void collectFunctions(ParseContext ctx, SimpleNode parent) {
    Map<String, FunctionDeclaration> functions = new HashMap();
    for (int ix = 0; ix < parent.jjtGetNumChildren(); ix++) {
      SimpleNode node = (SimpleNode) parent.jjtGetChild(ix);
      if (node.firstToken.kind != JsltParserConstants.DEF)
        continue;

      String name = node.jjtGetFirstToken().next.image;
      String[] params = collectParams(node);
      LetExpression[] lets = buildLets(ctx, node);

      SimpleNode expr = (SimpleNode) getLastChild(node);
      FunctionDeclaration func = new FunctionDeclaration(
        name, params, lets, node2expr(ctx, expr)
      );
      func.computeMatchContexts(null);
      ctx.addDeclaredFunction(name, func);
    }
  }

  private static String[] collectParams(SimpleNode node) {
    Token token = node.jjtGetFirstToken(); // DEF
    token = token.next; // IDENT
    token = token.next; // LPAREN

    List<String> params = new ArrayList();
    while (token.kind != JsltParserConstants.RPAREN) {
      if (token.kind == JsltParserConstants.IDENT)
        params.add(token.image);

      token = token.next;
    }

    return params.toArray(new String[0]);
  }

  private static ObjectExpression buildObject(ParseContext ctx, SimpleNode node) {
    LetExpression[] lets = buildLets(ctx, node);

    SimpleNode last = getLastChild(node);

    MatcherExpression matcher = collectMatcher(ctx, last);
    List<PairExpression> pairs = collectPairs(ctx, last);
    PairExpression[] children = new PairExpression[pairs.size()];
    children = pairs.toArray(children);

    return new ObjectExpression(lets, children, matcher,
                                makeLocation(ctx, node),
                                ctx.getObjectFilter());
  }

  private static MatcherExpression collectMatcher(ParseContext ctx,
                                                  SimpleNode node) {
    if (node == null)
      return null;

    SimpleNode last = getLastChild(node);
    if (node.id == JsltParserTreeConstants.JJTPAIR) {
      if (node.jjtGetNumChildren() == 2)
        return null; // last in chain was a pair

      return collectMatcher(ctx, last);
    } else if (node.id == JsltParserTreeConstants.JJTMATCHER) {
      List<String> minuses = new ArrayList();
      if (node.jjtGetNumChildren() == 2) // means there was "* - foo : ..."
        collectMinuses(ctx, getChild(node, 0), minuses);
      return new MatcherExpression(node2expr(ctx, last), minuses,
                                   makeLocation(ctx, last));
    } else if (node.id == JsltParserTreeConstants.JJTLET)
      return null; // last item is a let, which is messed up, but legal
    else
      throw new JsltException("INTERNAL ERROR: This is wrong: " + node);
  }

  private static void collectMinuses(ParseContext ctx, SimpleNode node,
                                     List<String> minuses) {
    Token token = node.jjtGetFirstToken();
    token = token.next; // skip the -

    while (true) {
      minuses.add(identOrString(ctx, token));
      token = token.next;
      if (token.kind == JsltParserConstants.COLON)
        break;
      // else: COMMA
      token = token.next;
    }
  }

  private static List<PairExpression> collectPairs(ParseContext ctx,
                                                   SimpleNode pair) {
    return collectPairs(ctx, pair, new ArrayList());
  }

  private static List<PairExpression> collectPairs(ParseContext ctx,
                                                   SimpleNode pair,
                                                   List<PairExpression> pairs) {
    if (pair != null && pair.id == JsltParserTreeConstants.JJTPAIR) {
      ExpressionNode key = node2expr(ctx, (SimpleNode) pair.jjtGetChild(0));
      ExpressionNode val = node2expr(ctx, (SimpleNode) pair.jjtGetChild(1));

      pairs.add(new PairExpression(key, val, makeLocation(ctx, pair)));
      if (pair.jjtGetNumChildren() > 1)
        collectPairs(ctx, getLastChild(pair), pairs);

      return pairs;
    } else
      // has to be a matcher, so we're done
      return pairs;
  }

  private static ObjectComprehension buildObjectComprehension(ParseContext ctx, SimpleNode node) {
    // children: loop-expr let* key-expr value-expr if-expr?
    ExpressionNode loopExpr = node2expr(ctx, getChild(node, 0));
    LetExpression[] lets = buildLets(ctx, node);

    int ix = lets.length + 1;

    ExpressionNode keyExpr = node2expr(ctx, getChild(node, ix));
    ExpressionNode valueExpr = node2expr(ctx, getChild(node, ix + 1));

    ExpressionNode ifExpr = null;
    if (node.jjtGetNumChildren() > lets.length + 3) // there is an if
      ifExpr = node2expr(ctx, getLastChild(node));

    return new ObjectComprehension(loopExpr, lets, keyExpr, valueExpr, ifExpr,
                                   makeLocation(ctx, node),
                                   ctx.getObjectFilter());
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
