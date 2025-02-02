package com.plasstech.lang.c.codegen;

public record Mov(AssemblyType type, Operand src, Operand dst) implements Instruction {
  @Override
  public String toString() {
    return String.format("mov%s %s, %s", type.suffix(), src, dst);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
