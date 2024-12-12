package com.plasstech.lang.c.codegen;

public record Pseudo(String identifier) implements Operand {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
