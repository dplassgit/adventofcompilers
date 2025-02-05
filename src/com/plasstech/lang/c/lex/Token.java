package com.plasstech.lang.c.lex;

import com.plasstech.lang.c.typecheck.Type;

public record Token(TokenType type, String value, Type varType) {
  public Token(TokenType type, String value) {
    this(type, value, Type.NO_TYPE);
  }

  public boolean isKeyword() {
    return type.isKeyword;
  }
}
