package com.plasstech.lang.c.parser;

public interface AstNodeVisitor<R> {
  <T> R visit(Constant<T> n);

  R visit(FunctionDef n);

  R visit(Program n);

  R visit(Return n);

  R visit(UnaryExp n);

  R visit(BinExp binExp);
}
