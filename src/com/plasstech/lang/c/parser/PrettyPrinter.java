package com.plasstech.lang.c.parser;

import com.google.common.base.Strings;

public class PrettyPrinter implements AstNodeVisitor {
  private int indentation;

  public void prettyPrint(Program p) {
    p.accept(this);
  }

  @Override
  public void visit(Program n) {
    System.out.println("Program (");
    indentation += 2;
    n.functionDef().accept(this);
    System.out.println(")");
    indentation -= 2;
  }

  @Override
  public <T> void visit(Constant<T> n) {
    System.out.printf("%sConstant(%s)\n", spaces(), n.toString());
  }

  @Override
  public void visit(UnaryExp n) {
    System.out.printf("%sUnary (\n", spaces());
    indentation += 2;
    System.out.printf("%soperator: %s\n", spaces(), n.operator().toString());
    System.out.printf("%sexp:\n", spaces());
    indentation += 2;
    n.exp().accept(this);
    indentation -= 4;
    System.out.printf("%s)\n", spaces());
  }

  @Override
  public void visit(FunctionDef n) {
    System.out.printf("%sFunction (\n", spaces());
    indentation += 2;
    System.out.printf("%sname: \"%s\"\n", spaces(), n.name());
    System.out.printf("%sbody:\n", spaces());
    indentation += 2;
    n.body().accept(this);
    indentation -= 4;
    System.out.printf("%s)\n", spaces());
  }

  @Override
  public void visit(Return n) {
    System.out.printf("%sReturn (\n", spaces());
    indentation += 2;
    n.expr().accept(this);
    indentation -= 2;
    System.out.printf("%s)\n", spaces());
  }

  private String spaces() {
    return Strings.repeat(" ", indentation);
  }
}
