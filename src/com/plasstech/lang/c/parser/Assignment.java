package com.plasstech.lang.c.parser;

/**
 * An assignment; right now lvalues can only be a Var(iable).
 */
public record Assignment(Exp lvalue, Exp rvalue) implements Exp {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
