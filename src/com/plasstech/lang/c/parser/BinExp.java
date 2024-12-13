package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.TokenType;

public record BinExp(Exp left, TokenType operator, Exp right) implements Exp {
  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
