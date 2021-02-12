
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

public class Utils {

  /**
   * Return a lower-case hex representation of the binary data.
   */
  public static String printHexBinary(byte[] data) {
    char[] buf = new char[data.length * 2];
    for (int ix = 0; ix < data.length; ix++) {
      buf[ix * 2] = getHexDigit((data[ix] >> 4) & 0x0F);
      buf[ix * 2 + 1] = getHexDigit(data[ix] & 0x0F);
    }
    return new String(buf, 0, buf.length);
  }

  private static char getHexDigit(int number) {
    if (number < 10)
      return (char) ('0' + number);
    else
      return (char) ('a' + (number - 10));
  }
}
