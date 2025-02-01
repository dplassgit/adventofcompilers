package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.typecheck.Type;

public record Pseudo(String identifier, Type type) implements Operand {
  @Override
  public boolean inMemory() {
    throw new IllegalStateException("Should not be asking Pseudo if it's in memory");
  }
}
