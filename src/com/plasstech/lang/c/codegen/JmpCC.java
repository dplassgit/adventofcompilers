package com.plasstech.lang.c.codegen;

public record JmpCC(CondCode cc, String label) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    // Page 89
    return String.format("j%s .L%s", cc().name().toLowerCase(), label());
  }
}
