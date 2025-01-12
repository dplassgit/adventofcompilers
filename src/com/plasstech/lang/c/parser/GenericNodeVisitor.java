package com.plasstech.lang.c.parser;

public class GenericNodeVisitor<R> implements AstNode.Visitor<R> {
  @Override
  public <T> R visit(Constant<T> n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(FunDecl n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Program n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Return n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(UnaryExp n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(BinExp n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Var n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Assignment n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Expression n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(NullStatement n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(VarDecl n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(If n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Conditional n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Block n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Compound n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Break n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Continue n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(DoWhile n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(For n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(While n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(InitDecl n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(InitExp n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(FunctionCall n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public R visit(Cast n) {
    throw new UnsupportedOperationException();
  }
}
