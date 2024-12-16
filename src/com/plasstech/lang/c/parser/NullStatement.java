package com.plasstech.lang.c.parser;

public record NullStatement() implements Statement {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
