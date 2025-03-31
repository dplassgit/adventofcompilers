package com.plasstech.lang.c.codegen;

public interface Operand {
  boolean inMemory();

  default String toString(int bytes) {
    return toString();
  }

  default String toString(AssemblyType type) {
    return switch (type) {
      case Longword -> toString(4);
      case Quadword, Double -> toString(8);
      default -> throw new IllegalArgumentException("Unexpected value: " + type);
    };
  }
}
