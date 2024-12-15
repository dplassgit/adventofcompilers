package com.plasstech.lang.c.codegen;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.plasstech.lang.c.lex.TokenType;

/** A conditional/flag code. */
public enum CondCode {
  E,
  NE,
  G,
  GE,
  L,
  LE;

  private static final Map<TokenType, CondCode> FROM_TOKEN_TYPE = ImmutableMap.of(
      TokenType.EQEQ, E,
      TokenType.NEQ, NE,
      TokenType.GT, G,
      TokenType.GEQ, GE,
      TokenType.LT, L,
      TokenType.LEQ, LE);

  public static CondCode from(TokenType operator) {
    return FROM_TOKEN_TYPE.get(operator);
  }
}
