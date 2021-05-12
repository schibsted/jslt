
package com.schibsted.spt.data.jslt.impl;

/**
 * Marker interface which can be implemented by a function to declare
 * that one of its parameters is a regular expression. That parameter
 * will then be evaluated at compile time to verify that the regular
 * expression is syntactically correct, provided it is a literal.
 */
public interface RegexpFunction {

    /**
     * Which argument in the argument list is a regexp?
     */
    public int regexpArgumentNumber();

}
