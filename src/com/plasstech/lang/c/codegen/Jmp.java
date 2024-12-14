package com.plasstech.lang.c.codegen;

public record Jmp(String label) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
