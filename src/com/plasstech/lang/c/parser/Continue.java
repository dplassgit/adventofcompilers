package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.common.Labelled;

public record Continue(String label) implements Statement, Labelled {
  public Continue() {
    this("defaultLabel");
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public final String toString() {
    return String.format("continue (%s)", label);
  }
}
