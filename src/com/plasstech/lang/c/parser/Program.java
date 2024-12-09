package com.plasstech.lang.c.parser;

public record Program(FunctionDef functionDef) implements AstNode {
  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
