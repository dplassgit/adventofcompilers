package com.plasstech.lang.c.typecheck;

import com.google.common.base.Joiner;

/**
 * Symbol for a symbol table.
 */
public record Symbol(String name, Type type, Attribute attribute) {
  @Override
  public final String toString() {
    return switch (type) {
      case FunType ft ->
        String.format("%s %s(%s)", ft.returnType().toString(), name(),
            Joiner.on(", ").join(ft.paramTypes().stream().map(Type::toString).toList()));

      default -> String.format("%s %s", type.name(), name);
    };
  }
}
