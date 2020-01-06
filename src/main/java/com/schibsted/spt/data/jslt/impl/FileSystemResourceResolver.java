
package com.schibsted.spt.data.jslt.impl;

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.ResourceResolver;

public final class FileSystemResourceResolver implements ResourceResolver {
    private Path rootPath;

    public FileSystemResourceResolver(Path rootPath) {
        this.rootPath = rootPath.toAbsolutePath();
    }

    public FileSystemResourceResolver() {
        this(FileSystems.getDefault().getPath("").toAbsolutePath());
    }

    @Override
    public Reader resolve(String jslt) {
      try {
        Path jsltPath = FileSystems.getDefault().getPath(jslt).normalize();
        if (!jsltPath.isAbsolute()) {
            jsltPath = rootPath.resolve(jsltPath).normalize();
        }

        InputStream is = Files.newInputStream(jsltPath, StandardOpenOption.READ);
        return new InputStreamReader(is);
      } catch (IOException e) {
        throw new JsltException("Could not resolve file '" + jslt + "': " + e, e);
      }
    }

}
