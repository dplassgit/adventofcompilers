package com.plasstech.lang.c.typecheck;

/**
 * A function type. For now it just tracks the number of params, which are assumed to be ints.
 */
public record FunType(int paramCount) implements Type {
  @Override
  public String name() {
    return "function type";
  }
}
