package com.plasstech.lang.c.typecheck;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.typecheck.Type.IntType;

/**
 * Symbol for a symbol table.
 */
public record Symbol(String name, Type type, Attribute attribute) {
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
