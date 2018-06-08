
package com.schibsted.spt.data.jstl2.impl;

import com.schibsted.spt.data.jstl2.Function;

/**
 * Interface to a module, which can come from loading a JSTL or
 * (perhaps in the future, from injecting collections of functions
 * etc).
 */
public interface Module {

  public Function getFunction(String name);

  // the module may also be callable, but we don't represent that part
  // of the functionality here

}
