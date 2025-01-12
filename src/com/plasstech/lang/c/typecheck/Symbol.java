package com.plasstech.lang.c.typecheck;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Joiner;

/**
 * Symbol for a symbol table.
 */
public record Symbol(String name, Type type, Attribute attribute) {
  @Override
  public final String toString() {
    return switch (type) {
      case FunType ft -> {
        List<String> ints = IntStream.range(0, ft.params().size()).mapToObj(x -> "int").toList();
        yield String.format("int %s(%s)", name,
            Joiner.on(", ").join(ints));
      }

      default -> String.format("%s %s", type.name(), name);
    };
  }
}
