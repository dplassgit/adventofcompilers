package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.TokenType;

public enum StorageClass {
  STATIC,
  EXTERN;

  public static StorageClass of(TokenType tt) {
    return switch (tt) {
      case STATIC -> STATIC;
      case EXTERN -> EXTERN;
      default -> throw new IllegalStateException("Unexpected token type " + tt);
    };
  }
}
