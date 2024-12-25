package com.plasstech.lang.c.parser;

public record Break() implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    return "break";
  }
}
