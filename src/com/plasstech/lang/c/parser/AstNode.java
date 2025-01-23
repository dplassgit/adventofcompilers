package com.plasstech.lang.c.parser;

public interface AstNode {
  interface Visitor<R> {
    R visit(Assignment n);

    R visit(BinExp n);

    R visit(Conditional n);

    <T extends Number> R visit(Constant<T> n);

    R visit(VarDecl n);

    R visit(Expression n);

    R visit(FunDecl n);

    R visit(If n);

    R visit(NullStatement n);

    R visit(Program n);

    R visit(Return n);

    R visit(UnaryExp n);

    R visit(Var n);

    R visit(Block n);

    R visit(Compound n);

    R visit(Break n);

    R visit(Continue n);

    R visit(DoWhile n);

    R visit(For n);

    R visit(While n);

    R visit(InitDecl n);

    R visit(InitExp n);

    R visit(FunctionCall n);

    R visit(Cast n);
  }

  /** Visitor pattern. */
  <T> T accept(Visitor<T> visitor);
}
