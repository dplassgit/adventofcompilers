package com.plasstech.lang.c.codegen;

public class Ret extends Instruction {
  @Override
  public String toString() {
    return "ret";
  }

  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }

}
