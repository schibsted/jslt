
package com.schibsted.spt.data.jslt;

import com.schibsted.spt.data.jstl2.impl.Location;

public class JsltException extends RuntimeException {
  private Location location;

  public JsltException(String message) {
    this(message, null, null);
  }

  public JsltException(String message, Location location) {
    this(message, null, location);
  }

  public JsltException(String message, Throwable cause) {
    this(message, cause, null);
  }

  public JsltException(String message, Throwable cause, Location location) {
    super(message, cause);
    this.location = location;
  }

  public String getMessage() {
    if (location != null)
      return super.getMessage() + " at " + location;
    else
      return super.getMessage();
  }

  public String getMessageWithoutLocation() {
    return super.getMessage();
  }

  public String getSource() {
    return location.getSource();
  }

  public int getLine() {
    return location.getLine();
  }

  public int getColumn() {
    return location.getColumn();
  }
}
