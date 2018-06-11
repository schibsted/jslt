
package com.schibsted.spt.data.jstl2;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.FunctionUtils;

/**
 * Test cases for the function wrapper implementations.
 */
public class FunctionWrapperTest extends TestBase {

  @Test
  public void testWrapStaticMethod() throws Exception {
    Collection<Function> functions = Collections.singleton(
      FunctionUtils.wrapStaticMethod("url-decode",
                                     "java.net.URLDecoder", "decode",
                                     new Class[] {String.class, String.class})
    );

    check("{}", "url-decode(\"foo\", \"utf-8\")", "\"foo\"",
          Collections.EMPTY_MAP,
          functions);
  }
}
