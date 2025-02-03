package com.plasstech.lang.c.codegen;

public record Label(String label) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    // Page 89
    return String.format(".L%s:", label());
  }
}
