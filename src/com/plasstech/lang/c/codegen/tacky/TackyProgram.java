package com.plasstech.lang.c.codegen.tacky;

public record TackyProgram(TackyFunctionDef functionDef) implements TackyNode {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
