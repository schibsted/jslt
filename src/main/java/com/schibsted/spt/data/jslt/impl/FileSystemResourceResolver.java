package com.schibsted.spt.data.jslt.impl;

import com.schibsted.spt.data.jslt.StreamResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileSystemResourceResolver extends StreamResourceResolver {

    private final Path rootPath;

    public FileSystemResourceResolver(final Path rootPath) {
        this.rootPath = rootPath.toAbsolutePath();
    }

    public FileSystemResourceResolver() {
        this(FileSystems.getDefault().getPath("").toAbsolutePath());
    }

    @Override
    protected InputStream getResourceAsStream(String jslt) throws IOException {
        Path jsltPath = FileSystems.getDefault().getPath(jslt).normalize();
        if (!jsltPath.isAbsolute()) {
            jsltPath = rootPath.resolve(jsltPath).normalize();
        }

        return Files.newInputStream(jsltPath, StandardOpenOption.READ);
    }

}
