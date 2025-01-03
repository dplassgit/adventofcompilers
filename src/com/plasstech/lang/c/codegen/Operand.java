package com.plasstech.lang.c.codegen;

public interface Operand {
  default String toString(int bytes) {
    return toString();
  }
}
