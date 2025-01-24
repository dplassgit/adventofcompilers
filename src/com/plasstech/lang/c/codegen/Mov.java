package com.plasstech.lang.c.codegen;

public record Mov(AssemblyType type, Operand src, Operand dest) implements Instruction {
  @Override
  public String toString() {
    return String.format("mov%s %s, %s", type.suffix(), src, dest);
    //    return String.format("mov%s %s, %s", "l", src, dest);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
