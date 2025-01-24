package com.plasstech.lang.c.codegen;

public record Cdq(AssemblyType type) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
