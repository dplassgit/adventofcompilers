package com.plasstech.lang.c.codegen;

public record Mov(AssemblyType type, Operand src, Operand dst) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
