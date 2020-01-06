package com.schibsted.spt.data.jslt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Given a string identifying a JSLT module file, return a Reader that
 * produces the module. This abstract class can be used to look up module
 * files other places than just on the classpath.
 */
public abstract class StreamResourceResolver extends ResourceResolver {

  protected abstract InputStream getResourceAsStream(final String jslt) throws IOException;

  public final Reader resolve(final String jslt, final Charset encoding) {
    try {
      final InputStream is = getResourceAsStream(jslt);
      if (is == null)
        throw new JsltException("Cannot load resource '" + jslt + "': not found");
      return new InputStreamReader(is, encoding);
    } catch (IOException e) {
      throw new JsltException("Couldn't load resource '" + jslt + "': " + e, e);
    }
  }

}
