package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.lex.TokenType;

public record AsmUnary(TokenType operator, Operand operand) implements Instruction {
  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }
}
