package com.plasstech.lang.c.codegen.tacky;

record TackyReturn(TackyVal val) implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
