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

package com.schibsted.jslt.extras.impl;

import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.impl.AbstractCallable;

/**
 * Contains JSLT functions for unwrapping/wrapping JWT tokens.
 * <p>
 * The api is (very!) loosely based on auth0/node-jsonwebtoken
 * <code>
 * from-jwt(token: String, [secretOrPublicKey], [options: Object])
 *
 * to-jwt(payload: JSON, [secretOrPrivateKey], [options: Object] )
 * </code>
 * secretOrPublicKey is a string or object containing either either the secret
 * for HMAC algorithms or the PEM encoded private key for RSA and ECDSA. If
 * this parameter is empty, the JWT token is assumed to use the "none"
 * algorithm which results in an empty signature component in the JWT.
 *
 */
public final class JWT {

  // This is copied from BuiltinFunctions.
  // this will be replaced with a proper Context. need to figure out
  // relationship between compile-time and run-time context first.
  public static Collection<Function> functions = new HashSet<Function>();
  static {
    functions.add(new JWT.FromJWT());
    functions.add(new JWT.ToJWT());
  }

  private static class FromJWT extends AbstractCallable implements Function {
    public FromJWT() {
      super("from-jwt", 1, 3);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode token = arguments[0];
      if (token.isNull())
        return NullNode.instance;
      else if (!token.isTextual())
        throw new JsltException("from-jwt(..) cannot extract a non-string: " + token);

      // TODO: Just for testing
      throw new JsltException("from-jwt(..) is unimplemented");
    }
  }

  private static class ToJWT extends AbstractCallable implements Function {
    public ToJWT() {
      super("from-jwt", 1, 3);
    }

    public JsonNode call(JsonNode input, JsonNode[] arguments) {
      JsonNode token = arguments[0];
      if (token.isNull())
        return NullNode.instance;
      else if (!token.isTextual())
        throw new JsltException("from-jwt(..) cannot extract a non-string: " + token);

      // TODO: Just for testing
      throw new JsltException("to-jwt-jwt(..) is unimplemented");
    }
  }

  private JWT() {
    //not called
  }
}
