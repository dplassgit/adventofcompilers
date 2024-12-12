package com.plasstech.lang.c.codegen;

public record Stack(int offset) implements Operand {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    return String.format("%d(%%rbp)", offset);
  }
}
