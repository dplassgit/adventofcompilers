package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.TokenType;

public record UnaryExp(TokenType operator, Exp exp) implements Exp {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
