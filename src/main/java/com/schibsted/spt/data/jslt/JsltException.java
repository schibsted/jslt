
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
    if (location == null)
      return null;
    else
      return location.getSource();
  }

  /**
   * What line did the error occur on? -1 if unknown.
   */
  public int getLine() {
    if (location == null)
      return -1;
    else
      return location.getLine();
  }

  /**
   * What column did the error occur on? -1 if unknown.
   */
  public int getColumn() {
    if (location == null)
      return -1;
    else
      return location.getColumn();
  }
}
