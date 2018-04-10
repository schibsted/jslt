
package com.schibsted.spt.data.jstl2;

import com.schibsted.spt.data.jstl2.impl.Location;

public class JstlException extends RuntimeException {
  private Location location;

  public JstlException(String message) {
    this(message, null, null);
  }

  public JstlException(String message, Location location) {
    this(message, null, location);
  }

  public JstlException(String message, Throwable cause) {
    this(message, cause, null);
  }

  public JstlException(String message, Throwable cause, Location location) {
    super(message, cause);
    this.location = location;
  }

  public String getMessage() {
    if (location != null)
      return super.getMessage() + " at " + location;
    else
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
