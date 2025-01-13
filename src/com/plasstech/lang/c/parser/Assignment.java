package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.typecheck.Type;

/**
 * An assignment; right now lvalues can only be a Var(iable).
 */
public record Assignment(Exp lvalue, Exp rvalue, Type type) implements Exp {
  public Assignment(Exp lvalue, Exp rvalue) {
    this(lvalue, rvalue, Type.NO_TYPE);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
