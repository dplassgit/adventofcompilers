package com.plasstech.lang.c.codegen;

public record Jmp(String label) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    // Page 89
    return String.format("jmp .L%s", label());
  }
}
