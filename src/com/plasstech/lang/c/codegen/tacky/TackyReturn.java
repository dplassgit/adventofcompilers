package com.plasstech.lang.c.codegen.tacky;

public class TackyReturn extends TackyInstruction {
  private final TackyVal val;

  public TackyReturn(TackyVal val) {
    this.val = val;
  }

  public TackyVal val() {
    return val;
  }

  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }

}
