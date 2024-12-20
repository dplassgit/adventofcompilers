package com.plasstech.lang.c.parser;

import java.util.Optional;

/**
 * Declaration of a variable, with an optional initial expression.
 */
public record Declaration(String identifier, Optional<Exp> init) implements BlockItem {
  public Declaration(String identifier) {
    this(identifier, Optional.empty());
  }

  public Declaration(String identifier, Exp init) {
    this(identifier, Optional.of(init));
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
