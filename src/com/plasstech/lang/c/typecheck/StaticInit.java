package com.plasstech.lang.c.typecheck;

public interface StaticInit {
  int bytes();

  long valueAsLong();

  // Name for the asm generation
  String name();
}
