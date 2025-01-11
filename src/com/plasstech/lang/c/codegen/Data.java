package com.plasstech.lang.c.codegen;

/**
 * Represents "RIP-relative" static data.
 */
public record Data(String identifier) implements Operand {
  @Override
  public final String toString() {
    return String.format("%s(%%rip)", identifier);
  }

  @Override
  public boolean inMemory() {
    return true;
  }
}
