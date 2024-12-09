package com.plasstech.lang.c.codegen.tacky;

public record TackyReturn(TackyVal val) implements TackyInstruction {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
