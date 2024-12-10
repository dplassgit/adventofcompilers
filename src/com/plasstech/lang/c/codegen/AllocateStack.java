package com.plasstech.lang.c.codegen;

public record AllocateStack(int bytes) implements Instruction {
  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }
}
