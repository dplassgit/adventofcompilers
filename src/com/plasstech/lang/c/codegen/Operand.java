package com.plasstech.lang.c.codegen;

public interface Operand {
  boolean inMemory();

  default String toString(int bytes) {
    return toString();
  }
}
