package com.plasstech.lang.c.codegen;

public record Call(String identifier) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
