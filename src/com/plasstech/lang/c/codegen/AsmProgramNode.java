package com.plasstech.lang.c.codegen;

public class AsmProgramNode extends AsmNode {
  private final AsmFunctionNode function;

  public AsmProgramNode(AsmFunctionNode function) {
    this.function = function;
  }

  public AsmFunctionNode function() {
    return function;
  }

  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }
}
