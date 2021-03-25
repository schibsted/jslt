
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

package com.schibsted.spt.data.jslt.impl;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import com.schibsted.spt.data.json.*;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.filters.JsonFilter;

public class ObjectExpression extends AbstractNode {
  private LetExpression[] lets;
  private PairExpression[] children;
  private DotExpression contextQuery; // find object to match
  private MatcherExpression matcher;
  private Set<String> keys; // the static keys defined in this template
  private JsonFilter filter;
  private boolean containsDynamicKeys;

  public ObjectExpression(LetExpression[] lets,
                          PairExpression[] children,
                          MatcherExpression matcher,
                          Location location,
                          JsonFilter filter) {
    super(location);
    this.lets = lets;
    this.children = children;
    this.matcher = matcher;
    this.filter = filter;

    this.keys = new HashSet();
    for (int ix = 0; ix < children.length; ix++) {
      if (children[ix].isKeyLiteral())
        keys.add(children[ix].getStaticKey());
      else {
        containsDynamicKeys = true;
        if (matcher != null)
          throw new JsltException("Object matcher not allowed in objects which have dynamic keys");
      }
    }
    if (matcher != null)
      for (String minus : matcher.getMinuses())
        keys.add(minus);

    if (!containsDynamicKeys)
      checkForDuplicates();
  }

  private void checkForDuplicates() {
    Set<String> seen = new HashSet(children.length);
    for (int ix = 0; ix < children.length; ix++) {
      if (seen.contains(children[ix].getStaticKey()))
        throw new JsltException("Invalid object declaration, duplicate key " +
                                "'" + children[ix].getStaticKey() + "'",
                                children[ix].getLocation());

      seen.add(children[ix].getStaticKey());
    }
  }

  public JsonValue apply(Scope scope, JsonValue input) {
    NodeUtils.evalLets(scope, input, lets);

    JsonObjectBuilder object = input.makeObjectBuilder();
    for (int ix = 0; ix < children.length; ix++) {
      JsonValue value = children[ix].apply(scope, input);
      if (filter.filter(value)) {
        String key = children[ix].applyKey(scope, input);

        if (containsDynamicKeys && object.has(key))
          throw new JsltException("Duplicate key '" + key + "' in object", children[ix].getLocation());

        object.set(key, value);
      }
    }

    if (matcher != null)
      evaluateMatcher(scope, input, object);

    return object.build();
  }

  private void evaluateMatcher(Scope scope, JsonValue input, JsonObjectBuilder object) {
    // find the object to match against
    JsonValue context = contextQuery.apply(scope, input);
    if (context.isNull() && !context.isObject())
      return; // no keys to match against

    // then do the matching
    Iterator<String> it = context.getKeys();
    while (it.hasNext()) {
      String key = it.next();
      if (keys.contains(key))
        continue; // the template has defined this key, so skip

      JsonValue value = matcher.apply(scope, context.get(key));
      object.set(key, value);
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
    for (int ix = 0; ix < lets.length; ix++)
      lets[ix].optimize();

    if (matcher != null)
      matcher.optimize();

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
    JsonValue object = apply(new OptimizerScope(), NullJValue.instance);
    return new LiteralExpression(object, location);
  }

  public void prepare(PreparationContext ctx) {
    ctx.scope.enterScope();

    for (int ix = 0; ix < lets.length; ix++) {
      lets[ix].register(ctx.scope);
    }

    for (ExpressionNode child : getChildren())
      child.prepare(ctx);

    ctx.scope.leaveScope();
  }

  public List<ExpressionNode> getChildren() {
    List<ExpressionNode> children = new ArrayList();
    children.addAll(Arrays.asList(lets));
    children.addAll(Arrays.asList(this.children));
    if (matcher != null)
      children.add(matcher);
    return children;
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
