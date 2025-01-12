package com.plasstech.lang.c.parser;

import com.google.common.base.Preconditions;

public record Constant<T>(T value) implements Exp {
  public static Constant<Integer> of(int value) {
    return new Constant<Integer>(value);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return value.toString();
  }

  public int asInt() {
    Preconditions.checkState(value instanceof Integer);
    return (int) value;
  }

  public long asLong() {
    Preconditions.checkState(value instanceof Long);
    return (long) value;
  }
}
