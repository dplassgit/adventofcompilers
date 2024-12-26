package com.plasstech.lang.c.parser;

public record While(String label, Exp condition, Statement body) implements Statement {
  public While(Exp condition, Statement body) {
    this("defaultLabel", condition, body);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
