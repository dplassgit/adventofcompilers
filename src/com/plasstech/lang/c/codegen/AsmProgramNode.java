package com.plasstech.lang.c.codegen;

public record AsmProgramNode(AsmFunctionNode function) implements AsmNode {
  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }
}
