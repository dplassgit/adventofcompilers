package com.plasstech.lang.c.codegen.tacky;

public record TackyIntConstant(int val) implements TackyVal {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
