package com.plasstech.lang.c.typecheck;

/**
 * Symbol for a symbol table. if "defined" is false, it means it was just declared. This applies for
 * functions mostly.
 */
public record Symbol(String name, Type type, boolean defined) {
  public Symbol(String name, Type type) {
    this(name, type, true);
  }
}
