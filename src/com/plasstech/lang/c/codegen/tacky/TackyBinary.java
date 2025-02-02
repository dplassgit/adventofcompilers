package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.lex.TokenType;

record TackyBinary(TackyVar dst, TackyVal left, TokenType operator, TackyVal right)
    implements TackyInstruction {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
