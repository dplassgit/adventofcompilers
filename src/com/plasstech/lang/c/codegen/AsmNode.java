package com.plasstech.lang.c.codegen;

/** 
 * Represents a "node" in the Asm tree.
 * <p>
 * Some nodes implement the `Instruction` interface, which extends `AsmNode`.
 */
public interface AsmNode {
  interface Visitor<R> {
    R visit(AllocateStack n);

    R visit(AsmFunctionNode n);

    R visit(AsmProgramNode n);

    R visit(AsmBinary n);

    R visit(AsmUnary n);

    R visit(Cdq n);

    R visit(Cmp n);

    R visit(Idiv n);

    R visit(Jmp n);

    R visit(JmpCC n);

    R visit(Mov n);

    R visit(Ret n);

    R visit(SetCC n);
  }

  <R> R accept(Visitor<R> visitor);
}
