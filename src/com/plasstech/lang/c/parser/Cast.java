package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.typecheck.Type;

public record Cast(Type targetType, Exp exp) implements Exp {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
