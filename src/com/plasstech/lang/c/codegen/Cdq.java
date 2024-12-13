package com.plasstech.lang.c.codegen;

public record Cdq() implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
