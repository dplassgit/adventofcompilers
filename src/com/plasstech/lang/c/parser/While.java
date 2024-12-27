package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.common.Labelled;

public record While(String label, Exp condition, Statement body) implements Statement, Labelled {
  public While(Exp condition, Statement body) {
    this("defaultLabel", condition, body);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
