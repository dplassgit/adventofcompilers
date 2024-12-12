package com.plasstech.lang.c.codegen.tacky;

/**
 * Represents an instruction.
 */
public interface TackyInstruction {
  public interface Visitor<R> {
    R visit(TackyUnary tackyUnaryOp);

    R visit(TackyReturn tackyReturn);
  }

  <R> R accept(Visitor<R> visitor);
}
