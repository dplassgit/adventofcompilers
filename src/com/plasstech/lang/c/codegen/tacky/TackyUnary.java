package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.lex.TokenType;

public record TackyUnary(TackyVar dest, TokenType operator, TackyVal src)
    implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
