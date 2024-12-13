package com.plasstech.lang.c.codegen;

public interface AsmNode {
  interface Visitor<R> {
    R visit(AsmProgramNode n);

    R visit(AsmFunctionNode n);

    R visit(Mov n);

    R visit(Ret n);

    R visit(AsmUnary n);

    R visit(AsmBinary n);

    R visit(Idiv n);

    R visit(Cdq n);

    R visit(AllocateStack n);
  }

  <R> R accept(Visitor<R> visitor);
}
