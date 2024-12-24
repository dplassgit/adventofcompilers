package com.plasstech.lang.c.parser;

import java.util.Optional;

public record InitExp(Optional<Exp> exp) implements ForInit {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
