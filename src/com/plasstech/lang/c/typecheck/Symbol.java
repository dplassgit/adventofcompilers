package com.plasstech.lang.c.typecheck;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.typecheck.Type.IntType;

/**
 * Symbol for a symbol table. if "defined" is false, it means it was just declared. This applies for
 * functions mostly.
 */
public record Symbol(String name, Type type, boolean defined) {
  public Symbol(String name, Type type) {
    this(name, type, true);
  }

  @Override
  public final String toString() {
    return switch (type) {
      case FunType ft -> {
        List<String> ints = IntStream.range(0, ft.paramCount()).mapToObj(x -> "int").toList();
        yield String.format("int %s(%s)", name,
            Joiner.on(", ").join(ints));
      }

      case IntType it -> String.format("%s %s", it.name(), name);
      default ->
        throw new IllegalArgumentException("Unexpected value: " + type);
    };
  }
}
