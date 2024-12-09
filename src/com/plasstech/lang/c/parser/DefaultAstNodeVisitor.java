package com.plasstech.lang.c.parser;

public class DefaultAstNodeVisitor implements AstNodeVisitor<Void> {

  @Override
  public <T> Void visit(Constant<T> n) {
    return null;
  }

  @Override
  public Void visit(FunctionDef n) {
    return null;
  }

  @Override
  public Void visit(Program n) {
    return null;
  }

  @Override
  public Void visit(Return n) {
    return null;
  }

  @Override
  public Void visit(UnaryExp n) {
    return null;
  }
}
