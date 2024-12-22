package com.plasstech.lang.c.parser;

public interface AstNode {
  interface Visitor<R> {
    R visit(Assignment n);

    R visit(BinExp n);

    R visit(Conditional n);

    <T> R visit(Constant<T> n);

    R visit(Declaration n);

    R visit(Expression n);

    R visit(FunctionDef n);

    R visit(If n);

    R visit(NullStatement n);

    R visit(Program n);

    R visit(Return n);

    R visit(UnaryExp n);

    R visit(Var n);

    R visit(Block n);

    R visit(Compound n);
  }

  /** Visitor pattern. */
  <T> T accept(Visitor<T> visitor);
}
