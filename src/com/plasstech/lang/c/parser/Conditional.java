package com.plasstech.lang.c.parser;

/**
 * Represents the ternary operator:
 * <p>
 * condition ? left : right
 */
public record Conditional(Exp condition, Exp left, Exp right) implements Exp {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
