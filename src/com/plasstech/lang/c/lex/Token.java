package com.plasstech.lang.c.lex;

public record Token(TokenType type, String value) {
  public boolean isKeyword() {
    return type.isKeyword;
  }

  public int asInt() {
    if (type == TokenType.INT_LITERAL) {
      return Integer.parseInt(value);
    }
    throw new IllegalStateException("Cannot get int of non-int literal");
  }
}
