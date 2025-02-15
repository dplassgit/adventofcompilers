package com.plasstech.lang.c.codegen;

/** Page 287 */
public record MovZeroExtend(Operand src, Operand dst) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
