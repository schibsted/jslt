
package com.schibsted.spt.data.jstl2.impl.vm;

import java.util.List;

public class Jump {
  private List<Integer> bytecode;
  private int ix;

  public Jump(List<Integer> bytecode) {
    this.bytecode = bytecode;
    this.ix = bytecode.size() - 1;
  }

  public void resolve() {
    bytecode.set(ix, bytecode.size());
  }
}
