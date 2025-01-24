package com.plasstech.lang.c.codegen;

/** Move with sign extension. Page 263 */
public record Movsx(Operand src, Operand dst) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
