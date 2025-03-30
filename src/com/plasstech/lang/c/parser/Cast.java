package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.typecheck.Type;

public record Cast(Type targetType, Exp exp) implements Exp {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public Type type() {
    return targetType();
  }

  @Override
  public String readableString() {
    return String.format("(%s) %s", targetType.toString(), exp.readableString());
  }
}
