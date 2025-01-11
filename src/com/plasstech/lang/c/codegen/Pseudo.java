package com.plasstech.lang.c.codegen;

public record Pseudo(String identifier) implements Operand {

  @Override
  public boolean inMemory() {
    throw new IllegalStateException("Should not be asking Pseudo if it's in memory");
  }
}
