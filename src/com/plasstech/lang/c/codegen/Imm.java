package com.plasstech.lang.c.codegen;

public record Imm(long value) implements Operand {
  @Override
  public String toString() {
    return String.format("$%s", value);
  }

  @Override
  public boolean inMemory() {
    return false;
  }
}
