package com.plasstech.lang.c.parser;

public class DefaultAstNodeVisitor implements AstNodeVisitor {

  @Override
  public <T> void visit(Constant<T> n) {}

  @Override
  public void visit(FunctionDef n) {}

  @Override
  public void visit(Program n) {}

  @Override
  public void visit(Return n) {}
}
