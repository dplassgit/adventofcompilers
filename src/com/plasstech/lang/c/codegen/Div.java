package com.plasstech.lang.c.codegen;

/** eax=eax/operand for unsigned */
public record Div(AssemblyType type, Operand operand) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    return String.format("div%s %s", type().suffix(), operand().toString(type()));
  }
}
