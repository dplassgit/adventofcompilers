package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.lex.TokenType;

public interface Type {
  String name();

  record SimpleType(String name) implements Type {
  }

  Type INT = new SimpleType("int");
  Type LONG = new SimpleType("long");

  static Type fromTokenType(TokenType type) {
    return switch (type) {
      case INT -> INT;
      case LONG -> LONG;
      default -> throw new IllegalArgumentException("Unexpected value: " + type);
    };
  }
}
