package com.plasstech.lang.c.parser;

public record Var(String identifier) implements AstNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
