package com.plasstech.lang.c.parser;

public record Constant<T>(T value) implements Exp {
  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
