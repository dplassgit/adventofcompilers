package com.plasstech.lang.c.parser;

/**
 * A compound statement has a block inside it. Page 135.
 */
public record Compound(Block block) implements Statement {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
