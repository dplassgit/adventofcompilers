package com.plasstech.lang.c.parser;

public record Break(String label) implements Statement {
  public Break() {
    this("defaultLabel");
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    return String.format("break (%s)", label);
  }
}
