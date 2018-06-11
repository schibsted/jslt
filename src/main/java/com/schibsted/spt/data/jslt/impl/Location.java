
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

/**
 * Represents a position in a JSTL source code file. Used in error
 * messages.
 */
public class Location {
  private String source; // can be null, if we don't know
  private int line;
  private int column;

  public Location(String source, int line, int column) {
    this.source = source;
    this.line = line;
    this.column = column;
  }

  public String getSource() {
    return source;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public String toString() {
    if (source != null)
      return source + ':' + line + ':' + column;
    else
      return "" + line + ':' + column;
  }
}
