package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.typecheck.Type;

/** Variable expression */
public record Var(String identifier, Type type) implements Exp {
  public Var(String identifier) {
    this(identifier, Type.NO_TYPE);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
