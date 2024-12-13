package com.plasstech.lang.c.codegen;

public record AsmProgramNode(AsmFunctionNode function) implements AsmNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
