package com.plasstech.lang.c.codegen.tacky;

/**
 * Represents an instruction.
 */
public interface TackyInstruction {
  interface Visitor<R> {
    R visit(TackyUnary op);

    R visit(TackyBinary op);

    R visit(TackyReturn op);
  }

  <R> R accept(Visitor<R> visitor);
}
