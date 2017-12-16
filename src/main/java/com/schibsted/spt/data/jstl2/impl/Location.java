
package com.schibsted.spt.data.jstl2.impl;

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

  public String toString() {
    if (source != null)
      return source + ':' + line + ':' + column;
    else
      return "" + line + ':' + column;
  }
}
