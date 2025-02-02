package com.plasstech.lang.c.codegen;

public record Push(AssemblyType type, Operand operand) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
