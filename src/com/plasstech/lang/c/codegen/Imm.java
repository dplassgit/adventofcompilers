package com.plasstech.lang.c.codegen;

public class Imm extends Operand {
  private final String value;

  public Imm(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("$%s", value);
  }
}
