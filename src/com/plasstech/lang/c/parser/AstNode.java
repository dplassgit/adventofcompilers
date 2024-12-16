package com.plasstech.lang.c.parser;

public interface AstNode {
  interface Visitor<R> {
    <T> R visit(Constant<T> n);

    R visit(FunctionDef n);

    R visit(Program n);

    R visit(Return n);

    R visit(UnaryExp n);

    R visit(BinExp binExp);

    R visit(Var n);

    R visit(Assignment n);

    R visit(Expression n);

    R visit(NullStatement n);

    R visit(Declaration n);
  }

  /** Visitor pattern. */
  <T> T accept(Visitor<T> visitor);
}
