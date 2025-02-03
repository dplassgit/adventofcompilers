package com.plasstech.lang.c.codegen;

public record Cdq(AssemblyType type) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    return switch (type()) {
      case Longword -> "cdq";
      case Quadword -> "cqo";
      default -> throw new IllegalArgumentException("Unexpected value: " + type());
    };
  }
}
