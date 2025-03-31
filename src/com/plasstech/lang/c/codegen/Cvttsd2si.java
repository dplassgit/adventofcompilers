package com.plasstech.lang.c.codegen;

/** Page 324 */
public record Cvttsd2si(AssemblyType dstType, Operand src, Operand dst) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
