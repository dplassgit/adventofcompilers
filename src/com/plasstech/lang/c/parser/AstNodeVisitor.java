package com.plasstech.lang.c.parser;

public interface AstNodeVisitor {
  <T> void visit(Constant<T> n);

  void visit(FunctionDef n);

  void visit(Program n);

  void visit(Return n);
}
