package com.plasstech.lang.c.codegen;

public record Mov(Operand src, Operand dest) implements Instruction {
  @Override
  public String toString() {
    // TODO this might have to be movq
    return String.format("movl %s, %s", src, dest);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
