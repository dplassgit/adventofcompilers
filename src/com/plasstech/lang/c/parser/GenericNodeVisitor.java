package com.plasstech.lang.c.parser;

public class GenericNodeVisitor<R> implements AstNode.Visitor<R> {
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

  @Override
  public R visit(Var n) {
    return null;
  }

  @Override
  public R visit(Assignment n) {
    return null;
  }

  @Override
  public R visit(Expression n) {
    return null;
  }

  @Override
  public R visit(NullStatement n) {
    return null;
  }

  @Override
  public R visit(Declaration n) {
    return null;
  }

  @Override
  public R visit(If n) {
    return null;
  }
}
