
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

import com.schibsted.spt.data.jslt.Function;

/**
 * Interface to a module, which can come from loading a JSTL or
 * (perhaps in the future, from injecting collections of functions
 * etc).
 */
public interface Module {

  public Function getFunction(String name);

  // the module may also be callable, but we don't represent that part
  // of the functionality here

}
