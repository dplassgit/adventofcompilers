package com.plasstech.lang.c.parser;

import java.util.Optional;

public record If(Exp condition, Statement then, Optional<Statement> elseStmt) implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
