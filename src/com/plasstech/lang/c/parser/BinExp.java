package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.Type;

public record BinExp(Exp left, TokenType operator, Exp right, Type type) implements Exp {
  public BinExp(Exp left, TokenType operator, Exp right) {
    this(left, operator, right, Type.NO_TYPE);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
