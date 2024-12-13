package com.plasstech.lang.c.codegen;

public record RegisterOperand(Register register) implements Operand {
  public enum Register {
    EAX,
    EDX,
    R10D,
    R11D
  }

  @Override
  public String toString() {
    return "%" + register.name().toLowerCase();
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
