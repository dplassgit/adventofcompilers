package com.plasstech.lang.c.codegen;

public interface AsmNodeVisitor<R> {
  R visit(AsmProgramNode n);

  R visit(AsmFunctionNode n);

  R visit(Mov n);

  R visit(Ret n);

  R visit(AsmUnary n);

  R visit(AllocateStack n);
}
