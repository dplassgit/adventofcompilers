package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.common.Labelled;

public record DoWhile(String label, Statement body, Exp condition) implements Statement, Labelled {
  public DoWhile(Statement body, Exp condition) {
    this("defaultLabel", body, condition);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
