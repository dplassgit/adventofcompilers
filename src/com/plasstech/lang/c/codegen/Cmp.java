package com.plasstech.lang.c.codegen;

public record Cmp(Operand left, Operand right) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
