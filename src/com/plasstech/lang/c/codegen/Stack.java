package com.plasstech.lang.c.codegen;

public record Stack(int offset) implements Operand {
  @Override
  public final String toString() {
    return String.format("%d(%%rbp)", offset);
  }

  @Override
  public boolean inMemory() {
    return true;
  }
}
