package com.plasstech.lang.c.codegen.tacky;

public class TackyProgram extends TackyNode {
  private final TackyFunctionDef functionDef;

  public TackyProgram(TackyFunctionDef functionDef) {
    this.functionDef = functionDef;
  }

  public TackyFunctionDef functionDef() {
    return functionDef;
  }

  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
