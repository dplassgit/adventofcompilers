package com.plasstech.lang.c.codegen;

public record Imm(String value) implements Operand {
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("$%s", value);
  }
}
