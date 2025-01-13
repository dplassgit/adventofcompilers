package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.typecheck.Type;

/**
 * Represents the ternary operator:
 * <p>
 * condition ? left : right
 */
public record Conditional(Exp condition, Exp left, Exp right, Type type) implements Exp {
  public Conditional(Exp condition, Exp left, Exp right) {
    this(condition, left, right, Type.NO_TYPE);
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
