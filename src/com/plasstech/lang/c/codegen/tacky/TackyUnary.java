package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.lex.TokenType;

public class TackyUnary extends TackyInstruction {
  private final TackyVal src;
  private final TackyVal dest;
  private final TokenType operator;

  public TackyUnary(TackyVal src, TackyVal dest, TokenType operator) {
    this.src = src;
    this.dest = dest;
    this.operator = operator;
  }

  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }

  public TackyVal src() {
    return src;
  }

  public TackyVal dest() {
    return dest;
  }

  public TokenType operator() {
    return operator;
  }
}
