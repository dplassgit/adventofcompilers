package com.plasstech.lang.c.codegen;

/** eax=eax/operand */
public record Idiv(AssemblyType type, Operand operand) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
