package com.plasstech.lang.c.codegen.tacky;

/**
 * Represents an instruction.
 */
interface TackyInstruction {
  interface Visitor<R> {
    R visit(TackyUnary op);

    R visit(TackyBinary op);

    R visit(TackyCopy op);

    R visit(TackyJump op);

    R visit(TackyJumpZero op);

    R visit(TackyJumpNotZero op);

    R visit(TackyLabel op);

    R visit(TackyReturn op);

    R visit(TackyFunCall op);

    R visit(TackySignExtend op);

    R visit(TackyTruncate op);
  }

  <R> R accept(Visitor<R> visitor);
}
