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
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
