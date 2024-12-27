package com.plasstech.lang.c.parser;

import java.util.List;

public record FunctionCall(String identifier, List<Exp> args) implements Exp {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
