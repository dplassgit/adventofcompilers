package com.plasstech.lang.c.codegen.tacky;

public record TackySignExtend(TackyVal src, TackyVal dst) implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
