package com.plasstech.lang.c.parser;

import com.google.common.base.Strings;

public class PrettyPrinter implements AstNode.Visitor<Void> {
  private int indentation;

  public Void prettyPrint(Program p) {
    p.accept(this);
    return null;
  }

  @Override
  public Void visit(Program n) {
    System.out.println("Program (");
    indentation += 2;
    n.functionDef().accept(this);
    System.out.println(")");
    indentation -= 2;
    return null;
  }

  @Override
  public <T> Void visit(Constant<T> n) {
    System.out.printf("%sConstant(%s)\n", spaces(), n.toString());
    return null;
  }

  @Override
  public Void visit(UnaryExp n) {
    System.out.printf("%sUnary (\n", spaces());
    indentation += 2;
    System.out.printf("%soperator: %s\n", spaces(), n.operator().toString());
    System.out.printf("%sexp:\n", spaces());
    indentation += 2;
    n.exp().accept(this);
    indentation -= 4;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(BinExp n) {
    System.out.printf("%sBinary (\n", spaces());
    indentation += 2;
    System.out.printf("%sleft:\n", spaces());
    indentation += 2;
    n.left().accept(this);
    indentation -= 2;
    System.out.printf("%soperator: %s\n", spaces(), n.operator().toString());
    System.out.printf("%sright:\n", spaces());
    indentation += 2;
    n.right().accept(this);
    indentation -= 4;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(FunctionDef n) {
    System.out.printf("%sFunction (\n", spaces());
    indentation += 2;
    System.out.printf("%sname: \"%s\"\n", spaces(), n.name());
    System.out.printf("%sbody:\n", spaces());
    indentation += 2;
    for (BlockItem item : n.body()) {
      item.accept(this);
    }
    indentation -= 4;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(Return n) {
    System.out.printf("%sReturn (\n", spaces());
    indentation += 2;
    n.exp().accept(this);
    indentation -= 2;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  private String spaces() {
    return Strings.repeat(" ", indentation);
  }

  @Override
  public Void visit(Var n) {
    System.out.printf("%sVar: %s\n", spaces(), n.identifier());
    return null;
  }

  @Override
  public Void visit(Assignment n) {
    System.out.printf("%sAssignment: (\n", spaces());
    indentation += 2;
    System.out.printf("%sleft:\n", spaces());
    indentation += 2;
    n.lvalue().accept(this);
    indentation -= 2;
    System.out.printf("%sright:\n", spaces());
    indentation += 2;
    n.rvalue().accept(this);
    indentation -= 4;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(Expression n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void visit(NullStatement n) {
    System.out.printf("%sNull statement\n", spaces());
    return null;
  }

  @Override
  public Void visit(Declaration n) {
    throw new UnsupportedOperationException();
  }
}
