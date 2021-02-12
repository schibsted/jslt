
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

import java.lang.reflect.Method;
import com.schibsted.spt.data.jslt.impl.FunctionWrapper;

/**
 * Useful methods for working with Functions.
 */
public class FunctionUtils {

  /**
   * Create a JSLT function from a static Java method. This will fail
   * if the method is overloaded.
   */
  static public Function wrapStaticMethod(String functionName,
                                          String className,
                                          String methodName)
    throws LinkageError, ExceptionInInitializerError, ClassNotFoundException {
    Class klass = Class.forName(className);
    Method[] methods = klass.getMethods();
    Method method = null;
    for (int ix = 0; ix < methods.length; ix++) {
      if (methods[ix].getName().equals(methodName)) {
        if (method == null)
          method = methods[ix];
        else
          throw new JsltException("More than one method named '" + methodName + "'");
      }
    }
    if (method == null)
      throw new JsltException("No such method: '" + methodName + "'");

    return new FunctionWrapper(functionName, method);
  }

  /**
   * Create a JSLT function from a static Java method.
   * @param paramTypes Array of types used to match overloaded methods.
   */
  static public Function wrapStaticMethod(String functionName,
                                          String className,
                                          String methodName,
                                          Class[] paramTypes)
    throws LinkageError, ExceptionInInitializerError, ClassNotFoundException,
           NoSuchMethodException {
    Class klass = Class.forName(className);
    Method method = klass.getMethod(methodName, paramTypes);
    return new FunctionWrapper(functionName, method);
  }
}
