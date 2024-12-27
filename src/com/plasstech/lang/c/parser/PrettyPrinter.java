package com.plasstech.lang.c.parser;

import com.google.common.base.Strings;

public class PrettyPrinter extends GenericNodeVisitor<Void> {
  private int indentation;

  public Void prettyPrint(Program p) {
    p.accept(this);
    return null;
  }

  @Override
  public Void visit(Program n) {
    System.out.println("Program (");
    indentation += 2;
    n.funDecls().stream().forEach(fd -> fd.accept(this));
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
  public Void visit(FunDecl n) {
    System.out.printf("%sFunction (\n", spaces());
    indentation += 2;
    System.out.printf("%sname: \"%s\"\n", spaces(), n.name());
    System.out.printf("%sbody:\n", spaces());
    indentation += 2;
    n.body().get().accept(this);
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
    System.out.printf("%sExpressionStatement: (\n", spaces());
    indentation += 2;
    n.exp().accept(this);
    indentation -= 2;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(NullStatement n) {
    System.out.printf("%sNull statement\n", spaces());
    return null;
  }

  @Override
  public Void visit(VarDecl n) {
    System.out.printf("%sDeclaration: (\n", spaces());

    indentation += 2;
    System.out.printf("%svariable: %s\n", spaces(), n.identifier());
    if (n.init().isPresent()) {
      System.out.printf("%sinit:\n", spaces());
      indentation += 2;
      n.init().get().accept(this);
      indentation -= 2;
    }
    indentation -= 2;
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(If n) {
    System.out.printf("%sIf: (\n", spaces());

    indentation += 2;
    System.out.printf("%scondition:\n", spaces());
    indentation += 2;
    n.condition().accept(this);

    indentation -= 2;
    System.out.printf("%sthen:\n", spaces());
    indentation += 2;
    n.then().accept(this);

    if (n.elseStmt().isPresent()) {
      indentation -= 2;
      System.out.printf("%selse:\n", spaces());
      indentation += 2;
      n.elseStmt().get().accept(this);
      indentation -= 2;
    } else {
      indentation -= 4;
    }
    System.out.printf("%s)\n", spaces());
    return null;
  }

  @Override
  public Void visit(Conditional n) {
    System.out.printf("%sConditional (\n", spaces());

    indentation += 2;
    System.out.printf("%scondition:\n", spaces());
    indentation += 2;
    n.condition().accept(this);

    indentation -= 2;
    System.out.printf("%sleft:\n", spaces());
    indentation += 2;
    n.left().accept(this);

    indentation -= 2;
    System.out.printf("%sright:\n", spaces());
    indentation += 2;
    n.right().accept(this);

    indentation -= 4;

    System.out.printf("%s)\n", spaces());
    return null;
  }
}
