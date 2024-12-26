package com.plasstech.lang.c.parser;

public record DoWhile(String label, Statement body, Exp condition) implements Statement {
  public DoWhile(Statement body, Exp condition) {
    this("defaultLabel", body, condition);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
