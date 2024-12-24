package com.plasstech.lang.c.parser;

public record While(Exp condition, Statement body) implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
