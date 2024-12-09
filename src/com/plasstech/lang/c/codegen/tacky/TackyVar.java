package com.plasstech.lang.c.codegen.tacky;

public record TackyVar(String identifier) implements TackyVal {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
