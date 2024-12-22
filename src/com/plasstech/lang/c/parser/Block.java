package com.plasstech.lang.c.parser;

import java.util.List;

/**
 * A block is a list of items. Page 135.
 */
public record Block(List<BlockItem> items) implements AstNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
