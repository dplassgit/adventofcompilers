package com.plasstech.lang.c.parser;

import java.util.List;

import com.plasstech.lang.c.typecheck.Type;

public record FunctionCall(String identifier, List<Exp> args, Type type) implements Exp {
  public FunctionCall(String identifier, List<Exp> args) {
    this(identifier, args, Type.NO_TYPE);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
