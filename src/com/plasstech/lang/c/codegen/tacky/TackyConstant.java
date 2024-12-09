package com.plasstech.lang.c.codegen.tacky;

public class TackyConstant extends TackyVal {
  private final int val;

  public TackyConstant(int val) {
    this.val = val;
  }

  public int val() {
    return val;
  }

  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
