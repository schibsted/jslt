
package com.schibsted.spt.data.jslt;

import java.lang.reflect.Method;
import com.schibsted.spt.data.jslt.impl.FunctionWrapper;

public class FunctionUtils {

  static public Function wrapStaticMethod(String functionName,
                                          String className,
                                          String methodName)
    throws LinkageError, ExceptionInInitializerError, ClassNotFoundException {
    Class klass = Class.forName(className);
    Method[] methods = klass.getMethods();
    Method method = null;
    for (int ix = 0; ix < methods.length; ix++)
      if (methods[ix].getName().equals(methodName)) {
        if (method == null)
          method = methods[ix];
        else
          throw new JsltException("More than one method named '" + methodName + "'");
      }

    return new FunctionWrapper(functionName, method);
  }

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
