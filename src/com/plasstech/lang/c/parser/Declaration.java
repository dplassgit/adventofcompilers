package com.plasstech.lang.c.parser;

import java.util.Optional;

/**
 * Declaration of a variable, with an optional initial expression.
 */
public record Declaration(String identifier, Optional<Exp> init) implements BlockItem {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
