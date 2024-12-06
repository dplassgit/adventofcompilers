package com.plasstech.lang.c.parser;

public class Constant<T> extends Exp {
  private final T value;

  public Constant(T value) {
    this.value = value;
  }

  public T value() {
    return value;
  }

  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
