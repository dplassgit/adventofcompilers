package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.lex.TokenType;

public record TackyUnary(TackyVar dest, TokenType operator, TackyVal src)
    implements TackyInstruction {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
