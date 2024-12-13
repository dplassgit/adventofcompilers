package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.lex.TokenType;

public record TackyBinary(TackyVar dest, TokenType operator, TackyVal src1, TackyVal src2)
    implements TackyInstruction {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
