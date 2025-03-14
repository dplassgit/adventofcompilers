package com.plasstech.lang.c.lex;

import com.plasstech.lang.c.typecheck.Type;

public record Token(TokenType type, String value, Type varType, Position pos) {
  public Token(TokenType type, String value, Position pos) {
    this(type, value, Type.NO_TYPE, pos);
  }

  public boolean isKeyword() {
    return type.isKeyword;
  }
}
