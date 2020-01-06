
package com.schibsted.spt.data.jslt.impl;

import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.ResourceResolver;

public final class FileSystemResourceResolver implements ResourceResolver {
  private File rootPath; // can be null
  private Charset charset;

  public FileSystemResourceResolver(File rootPath, Charset charset) {
    this.rootPath = rootPath;
    this.charset = charset;
  }

  public FileSystemResourceResolver(File rootPath) {
    this(rootPath, StandardCharsets.UTF_8);
  }

  public FileSystemResourceResolver(Path rootPath, Charset charset) {
    this.rootPath = rootPath.toAbsolutePath().toFile();
    this.charset = charset;
  }

  public FileSystemResourceResolver(Path rootPath) {
    this(rootPath.toFile(), StandardCharsets.UTF_8);
  }

  public FileSystemResourceResolver(Charset charset) {
    this((File) null, charset);
  }

  public FileSystemResourceResolver() {
    this((File) null, StandardCharsets.UTF_8);
  }

  @Override
  public Reader resolve(String jslt) {
    try {
      File file = new File(rootPath, jslt);
      InputStream is = new FileInputStream(file);
      return new InputStreamReader(is, charset);
    } catch (IOException e) {
      throw new JsltException("Could not resolve file '" + jslt + "': " + e, e);
    }
  }
}
