package com.plasstech.lang.c.codegen;

public record Imm(String value) implements Operand {
  public Imm(int value) {
    this(Integer.toString(value));
  }

  @Override
  public String toString() {
    return String.format("$%s", value);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
