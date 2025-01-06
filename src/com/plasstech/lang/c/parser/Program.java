package com.plasstech.lang.c.parser;

import java.util.List;

public record Program(List<FunDecl> declarations) implements AstNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
