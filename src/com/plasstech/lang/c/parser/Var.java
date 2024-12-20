package com.plasstech.lang.c.parser;

/** Variable expression */
public record Var(String identifier) implements Exp {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
