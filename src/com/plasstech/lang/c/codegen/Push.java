package com.plasstech.lang.c.codegen;

public record Push(Operand operand) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return String.format("pushq %s", operand().toString(8));
  }
}
