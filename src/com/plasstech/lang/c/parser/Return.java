package com.plasstech.lang.c.parser;

public class Return extends Statement {
  private final Exp expr;

  public Return(Exp expr) {
    this.expr = expr;
  }

  public Exp expr() {
    return expr;
  }

  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visit(this);
  }
}
