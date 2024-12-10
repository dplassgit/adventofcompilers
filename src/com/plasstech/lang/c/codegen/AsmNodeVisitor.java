package com.plasstech.lang.c.codegen;

public interface AsmNodeVisitor {
  void visit(AsmProgramNode n);

  void visit(AsmFunctionNode n);

  void visit(Mov n);

  void visit(Ret n);

  void visit(AsmUnary asmUnary);

  void visit(AllocateStack allocateStack);
}
