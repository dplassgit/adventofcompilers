package com.plasstech.lang.c.codegen;

public record Ret() implements Instruction {
  @Override
  public String toString() {
    return "ret";
  }

  @Override
  public <R> R accept(AsmNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
