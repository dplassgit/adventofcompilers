package com.plasstech.lang.c.codegen.tacky;

public record TackyReturn(TackyVal val) implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
