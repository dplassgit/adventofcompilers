package com.plasstech.lang.c.parser;

public record Return(Exp expr) implements Statement {
  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
