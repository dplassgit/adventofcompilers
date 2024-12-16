package com.plasstech.lang.c.parser;

/**
 * An expression that's also a statement. Not sure how/why this is different than exp...
 */
public record Expression(Exp exp) implements Statement {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
