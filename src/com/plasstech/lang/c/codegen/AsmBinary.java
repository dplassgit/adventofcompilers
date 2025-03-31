package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.lex.TokenType;

/**
 * Binary operation, e.g.,
 * <p>
 * addl $2, -4(%rbp)
 * 
 * Page 62 et al
 */
public record AsmBinary(TokenType operator, AssemblyType type, Operand src, Operand dst)
    implements Instruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    String instruction = switch (operator) {
      case MINUS -> "sub";
      case PLUS -> "add";
      case STAR -> "imul";
      case HAT -> "xor";
      case SLASH -> {
        // should only be for double types
        if (!type.equals(AssemblyType.Double)) {
          throw new IllegalStateException("Must use this version of / with doubles");
        }
        yield "div";
      }
      default -> throw new IllegalStateException("Bad binary operator " + operator.name());
    };
    // Suffix added page 270
    return String.format("%s%s %s, %s", instruction, type.suffix(), src.toString(type()),
        dst.toString(type()));
  }
}
