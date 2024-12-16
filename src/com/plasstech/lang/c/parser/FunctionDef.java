package com.plasstech.lang.c.parser;

import java.util.List;

public record FunctionDef(String name, List<BlockItem> body) implements AstNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
