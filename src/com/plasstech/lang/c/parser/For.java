package com.plasstech.lang.c.parser;

import java.util.Optional;

public record For(ForInit init, Optional<Exp> condition, Optional<Exp> post, Statement body)
    implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
