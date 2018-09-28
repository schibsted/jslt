
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

import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.ResourceResolver;

public class ClasspathResourceResolver implements ResourceResolver {

  public Reader resolve(String jslt) {
    try {
      InputStream is = getClass().getClassLoader().getResourceAsStream(jslt);
      if (is == null)
        throw new JsltException("Cannot load resource '" + jslt + "': not found");
      return new InputStreamReader(is, "utf-8");
    } catch (IOException e) {
      throw new JsltException("Couldn't load resource '" + jslt + "': " + e, e);
    }
  }
}
