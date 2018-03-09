
package com.schibsted.spt.data.jstl2.impl;

import com.schibsted.spt.data.jstl2.JstlException;

/**
 * These expression cannot be generated in the syntax, but are used to
 * mark that an object matcher (* : .) is being used inside an array,
 * which is not allowed. The computeMatchContexts() method in
 * ArrayExpression will inject a FailDotExpression, which is later
 * used to detect that the matcher is used in an illegal location.
 */
public class FailDotExpression extends DotExpression {

  public FailDotExpression(Location location) {
    super(location);
  }

  // verify that we've build a correct DotExpression for our object
  // matcher (only used for that)
  public void checkOk(Location matcher) {
    // we're actually being used. this is illegal!
    throw new JstlException("Object matcher used inside array", matcher);
  }
}
