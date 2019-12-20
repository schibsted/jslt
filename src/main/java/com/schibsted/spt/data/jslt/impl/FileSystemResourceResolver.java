
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

package com.schibsted.spt.data.jslt.impl;

import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.ResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.*;

public class FileSystemResourceResolver implements ResourceResolver {

    private final Path rootPath;

    public FileSystemResourceResolver(final Path rootPath) {
        this.rootPath = rootPath;
    }

    public FileSystemResourceResolver() {
        this(FileSystems.getDefault().getPath("").toAbsolutePath());
    }

    public Reader resolve(String jslt) {

        Path jsltPath = FileSystems.getDefault().getPath(jslt).normalize();
        if (!jsltPath.isAbsolute()) {
          jsltPath = rootPath.resolve(jsltPath).normalize();
        }

        try {
            final InputStream is = Files.newInputStream(jsltPath, StandardOpenOption.READ);
            if (is == null) {
                throw new JsltException("Cannot load '" + jslt + "' from file system: not found");
            }
            return new InputStreamReader(is, "utf-8");
        } catch (IOException e) {
            throw new JsltException("Couldn't load '" + jslt + "' from file system: " + e, e);
        }
    }
}
