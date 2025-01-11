package com.plasstech.lang.c.codegen;

/**
 * Represents "RIP-relative" static data.
 */
public record Data(String identifier) implements Operand {
  @Override
  public boolean inMemory() {
    return true;
  }
}
