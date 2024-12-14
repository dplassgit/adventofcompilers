package com.plasstech.lang.c.codegen;

public record SetCC(CondCode cc, Operand dest) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
