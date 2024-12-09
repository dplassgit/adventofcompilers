package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.TokenType;

public class UnaryExp extends Exp {
  private final TokenType operator;
  private final Exp exp;

  // Should this take a new enum? /shrug.
  public UnaryExp(TokenType operator, Exp exp) {
    this.operator = operator;
    this.exp = exp;
  }

  public TokenType operator() {
    return operator;
  }

  public Exp exp() {
    return exp;
  }

  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
