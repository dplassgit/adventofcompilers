package com.plasstech.lang.c.parser;

import java.util.Optional;

public record For(String label, ForInit init, Optional<Exp> condition, Optional<Exp> post,
    Statement body)
    implements Statement {
  public For(ForInit init, Optional<Exp> condition, Optional<Exp> post, Statement body) {
    this("defaultLabel", init, condition, post, body);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
