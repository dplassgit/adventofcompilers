package com.plasstech.lang.c.codegen;

public interface AsmNode {
  interface Visitor<R> {
    R visit(AsmProgram op);

    R visit(AsmFunction op);

    R visit(AsmStaticVariable op);

    R visit(Mov op);

    R visit(AsmUnary op);

    R visit(AsmBinary op);

    R visit(Cmp op);

    R visit(Idiv op);

    R visit(Cdq op);

    R visit(Jmp op);

    R visit(JmpCC op);

    R visit(SetCC op);

    R visit(Label op);

    R visit(AllocateStack op);

    R visit(Ret op);

    R visit(DeallocateStack op);

    R visit(Push op);

    R visit(Call op);

    R visit(Movsx op);
  }

  <R> R accept(Visitor<R> visitor);
}
