package com.plasstech.lang.c.typecheck;

import java.util.List;

/**
 * A function type. For now it just tracks the number of params, which are assumed to be ints.
 */
public record FunType(Type returnType, List<Type> paramTypes) implements Type {
  @Override
  public String name() {
    return "function type";
  }
}
