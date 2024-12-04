package com.plasstech.lang.c.lex;

public record Token(TokenType type, String value) {
  public boolean isKeyword() {
    return type.isKeyword;
  }
}
