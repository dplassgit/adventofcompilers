package com.plasstech.lang.c.codegen;

public record Ret() implements Instruction {
  @Override
  public String toString() {
    return "ret";
  }

  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }

}
