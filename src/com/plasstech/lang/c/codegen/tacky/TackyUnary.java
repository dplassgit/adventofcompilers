package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.lex.TokenType;

public record TackyUnary(TackyVal src, TackyVal dest, TokenType operator)
    implements TackyInstruction {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
