
package com.schibsted.spt.data.jslt;

import com.schibsted.spt.data.jslt.impl.Location;

/**
 * Parent exception for all exceptions thrown by JSLT for both
 * compilation and run-time errors.
 */
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

  /**
   * Returns the error message with location information.
   */
  public String getMessage() {
    if (location != null)
      return super.getMessage() + " at " + location;
    else
      return super.getMessage();
  }

  /**
   * Returns the error message without location information.
   */
  public String getMessageWithoutLocation() {
    return super.getMessage();
  }

  /**
   * What file/resource did the error occur in? Can be null.
   */
  public String getSource() {
    return location.getSource();
  }

  /**
   * What line did the error occur on? -1 if unknown.
   */
  public int getLine() {
    return location.getLine();
  }

  /**
   * What column did the error occur on? -1 if unknown.
   */
  public int getColumn() {
    return location.getColumn();
  }
}
