package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.lex.TokenType;

/**
 * Binary operation, e.g.,
 * <p>
 * addl $2, -4(%rbp)
 */
public record AsmBinary(TokenType operator, AssemblyType type, Operand left, Operand right)
    implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    String instruction = switch (operator) {
      case MINUS -> "sub" + type.suffix();
      case PLUS -> "add" + type.suffix();
      case STAR -> "imul" + type.suffix();
      default -> throw new IllegalStateException("Bad binary operator " + operator.name());
    };
    return String.format("%s %s, %s", instruction, left.toString(), right.toString());
  }
}
