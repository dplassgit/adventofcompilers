package com.plasstech.lang.c.codegen.tacky;

record TackyCopy(TackyVal src, TackyVar dst) implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
