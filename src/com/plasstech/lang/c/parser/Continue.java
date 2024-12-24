package com.plasstech.lang.c.parser;

public record Continue() implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
