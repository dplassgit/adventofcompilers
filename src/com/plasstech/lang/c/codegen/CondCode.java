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
  LE,
  // Page 287
  A,
  AE,
  B,
  BE;

  private static final Map<TokenType, CondCode> FROM_TOKEN_TYPE = ImmutableMap.of(
      TokenType.EQEQ, E,
      TokenType.NEQ, NE,
      TokenType.GT, G,
      TokenType.GEQ, GE,
      TokenType.LT, L,
      TokenType.LEQ, LE);

  // Page 289
  private static final Map<TokenType, CondCode> FROM_TOKEN_TYPE_UNSIGNED = ImmutableMap.of(
      TokenType.EQEQ, E,
      TokenType.NEQ, NE,
      TokenType.GT, A,
      TokenType.GEQ, AE,
      TokenType.LT, B,
      TokenType.LEQ, BE);

  public static CondCode from(TokenType operator, boolean signed) {
    if (signed) {
      return FROM_TOKEN_TYPE.get(operator);
    } else {
      return FROM_TOKEN_TYPE_UNSIGNED.get(operator);
    }
  }
}
