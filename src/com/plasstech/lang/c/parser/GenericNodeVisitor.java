package com.plasstech.lang.c.parser;

public abstract class GenericNodeVisitor<R> implements AstNodeVisitor<R> {
  @Override
  public <T> R visit(Constant<T> n) {
    return null;
  }

  @Override
  public R visit(FunctionDef n) {
    return null;
  }

  @Override
  public R visit(Program n) {
    return null;
  }

  @Override
  public R visit(Return n) {
    return null;
  }

  @Override
  public R visit(UnaryExp n) {
    return null;
  }

  @Override
  public R visit(BinExp n) {
    return null;
  }
}
