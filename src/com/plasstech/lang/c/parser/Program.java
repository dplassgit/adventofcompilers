package com.plasstech.lang.c.parser;

public class Program extends AstNode {
  private final FunctionDef functionDef;

  public Program(FunctionDef functionDef) {
    this.functionDef = functionDef;
  }

  public FunctionDef functionDef() {
    return functionDef;
  }

  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visit(this);
  }
}
