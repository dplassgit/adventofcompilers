package com.plasstech.lang.c.parser;

public record DoWhile(Statement body, Exp condition) implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
