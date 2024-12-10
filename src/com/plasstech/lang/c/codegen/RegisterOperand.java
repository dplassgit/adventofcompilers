package com.plasstech.lang.c.codegen;

public record RegisterOperand(Register register) implements Operand {
  public enum Register {
    EAX,
    R10D
  }

  @Override
  public String toString() {
    return "%" + register.name().toLowerCase();
  }
}
