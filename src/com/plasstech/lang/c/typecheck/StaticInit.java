package com.plasstech.lang.c.typecheck;

public interface StaticInit {
  int bytes();

  default long valueAsLong() {
    throw new IllegalStateException("Cannot get value as long");
  }

  default double valueAsDouble() {
    throw new IllegalStateException("Cannot get value as double");
  }

  // Name for the asm generation
  String name();
}
