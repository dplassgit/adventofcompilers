package com.plasstech.lang.c.parser;

import java.util.List;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.typecheck.Type;

public record FunctionCall(String identifier, List<Exp> args, Type type) implements Exp {
  public FunctionCall(String identifier, List<Exp> args) {
    this(identifier, args, Type.NO_TYPE);
  }

  public Type returnType() {
    return type(); // heh.
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String readableString() {
    return String.format("%s(%s)", identifier,
        Joiner.on(", ").join(args.stream().map(Exp::readableString).toList()));
  }
}
