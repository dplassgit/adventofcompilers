package com.plasstech.lang.c.typecheck;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * A function type. For now it just tracks the number of params, which are assumed to be ints.
 */
public record FunType(Type returnType, List<Type> paramTypes) implements Type {
  @Override
  public final String toString() {
    return String.format("%s function(%s)", returnType.toString(),
        Joiner.on(",").join(paramTypes.stream().map(Type::toString).toList()));
  }

  @Override
  public String name() {
    return "function type";
  }
}
