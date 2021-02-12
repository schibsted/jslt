
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

import com.schibsted.spt.data.jslt.JsltException;

/**
 * These expression cannot be generated in the syntax, but are used to
 * mark that an object matcher (* : .) is being used inside an array,
 * which is not allowed. The computeMatchContexts() method in
 * ArrayExpression will inject a FailDotExpression, which is later
 * used to detect that the matcher is used in an illegal location.
 */
public class FailDotExpression extends DotExpression {
  private String where;

  public FailDotExpression(Location location, String where) {
    super(location);
    this.where = where;
  }

  // verify that we've build a correct DotExpression for our object
  // matcher (only used for that)
  public void checkOk(Location matcher) {
    // we're actually being used. this is illegal!
    throw new JsltException("Object matcher used inside " + where, matcher);
  }
}
