package com.plasstech.lang.c.codegen.tacky;

// Combine with TackyJumpZero?
record TackyJumpNotZero(TackyVal condition, String target) implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
