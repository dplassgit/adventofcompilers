package com.plasstech.lang.c.parser;

import com.google.common.base.Preconditions;

public record Constant<T>(T value) implements Exp {
  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return value.toString();
  }

  public int asInt() {
    Preconditions.checkState(value instanceof Integer);
    return (Integer) value;
  }
}
