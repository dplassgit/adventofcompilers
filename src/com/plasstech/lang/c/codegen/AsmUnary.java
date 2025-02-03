package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.lex.TokenType;

public record AsmUnary(TokenType operator, AssemblyType type, Operand operand)
    implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    String instruction = switch (operator()) {
      case MINUS -> "neg";
      case TWIDDLE -> "not";
      default -> throw new IllegalStateException("Bad unary operator " + operator());
    };
    // Suffix added page 270
    return String.format("%s%s %s", instruction, type().suffix(), operand().toString(type()));
  }
}
