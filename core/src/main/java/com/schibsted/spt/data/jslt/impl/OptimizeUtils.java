
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

public class OptimizeUtils {

  /**
   * Removes let expressions for variables that are simply assigned to
   * literals, because VariableExpression will inline those literals
   * and remove itself, so there's no need to evaluate the variable.
   */
  public static LetExpression[] optimizeLets(LetExpression[] lets) {
    int count = 0;
    for (int ix = 0; ix < lets.length; ix++) {
      lets[ix].optimize();
      if (!(lets[ix].getDeclaration() instanceof LiteralExpression))
        count++;
    }

    if (count == lets.length)
      return lets;

    LetExpression[] filtered = new LetExpression[count];
    int pos = 0;
    for (int ix = 0; ix < lets.length; ix++) {
      if (!(lets[ix].getDeclaration() instanceof LiteralExpression))
        filtered[pos++] = lets[ix];
    }
    return filtered;
  }
}
