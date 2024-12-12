package com.plasstech.lang.c.codegen.tacky;

public record TackyIntConstant(int val) implements TackyVal {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
