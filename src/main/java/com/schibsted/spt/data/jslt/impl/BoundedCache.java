
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

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * A Map implementation that deletes the oldest entry once the size
 * goes over a preset limit. This prevents the Map from growing
 * without bounds.
 */
public class BoundedCache<K, V> extends LinkedHashMap<K, V> {
  private int maxSize;

  public BoundedCache(int maxSize) {
    this.maxSize = maxSize;
  }

  protected boolean removeEldestEntry(Map.Entry eldest) {
    return size() > maxSize;
  }
}
