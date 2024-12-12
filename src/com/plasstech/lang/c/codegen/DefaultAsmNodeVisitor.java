package com.plasstech.lang.c.codegen;

public abstract class DefaultAsmNodeVisitor<R> implements AsmNodeVisitor<R> {
  @Override
  public R visit(AsmProgramNode n) {
    return null;
  }

  @Override
  public R visit(AsmFunctionNode n) {
    return null;
  }

  @Override
  public R visit(Mov n) {
    return null;
  }

  @Override
  public R visit(Ret n) {
    return null;
  }

  @Override
  public R visit(AsmUnary n) {
    return null;
  }

  @Override
  public R visit(AllocateStack n) {
    return null;
  }
}
