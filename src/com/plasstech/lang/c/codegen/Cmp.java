package com.plasstech.lang.c.codegen;

public record Cmp(AssemblyType type, Operand left, Operand right) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    // Page 89
    return String.format("cmp%s %s, %s", type().suffix(), left().toString(type()),
        right().toString(type()));
  }
}
