
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

import java.io.Reader;

/**
 * Given a string identifying a JSLT module file, return a Reader that
 * produces the module. This abstract class can be used to look up module
 * files other places than just on the classpath.
 */
public interface ResourceResolver {

  /**
   * Return a Reader for the given module.
   */
  public Reader resolve(String jslt);

}
