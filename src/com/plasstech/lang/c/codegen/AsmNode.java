package com.plasstech.lang.c.codegen;

public interface AsmNode {
  interface Visitor<R> {
    R visit(AsmProgramNode n);

    R visit(AsmFunctionNode n);

    R visit(AsmStaticVariable n);

    R visit(Mov n);

    R visit(AsmUnary n);

    R visit(AsmBinary n);

    R visit(Cmp n);

    R visit(Idiv n);

    R visit(Cdq n);

    R visit(Jmp n);

    R visit(JmpCC n);

    R visit(SetCC n);

    R visit(Label n);

    R visit(AllocateStack n);

    R visit(Ret n);

    R visit(DeallocateStack n);

    R visit(Push n);

    R visit(Call n);
  }

  <R> R accept(Visitor<R> visitor);
}
