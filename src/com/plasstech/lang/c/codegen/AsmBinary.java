package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.lex.TokenType;

/**
 * Binary operation, e.g.,
 * <p>
 * addl $2, -4(%rbp)
 */
public record AsmBinary(TokenType operator, Operand left, Operand right) implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
