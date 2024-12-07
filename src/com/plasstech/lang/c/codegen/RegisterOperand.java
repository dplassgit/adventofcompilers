package com.plasstech.lang.c.codegen;

public class RegisterOperand extends Operand {
  public enum Register {
    RAX
  }

  private final Register register;

  public RegisterOperand(Register register) {
    this.register = register;
  }

  public Register register() {
    return register;
  }

  @Override
  public String toString() {
    return "%" + register.name().toLowerCase();
  }
}
