package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.Type;

public record UnaryExp(TokenType operator, Exp exp, Type type) implements Exp {
  public UnaryExp(TokenType operator, Exp exp) {
    this(operator, exp, Type.NO_TYPE);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
