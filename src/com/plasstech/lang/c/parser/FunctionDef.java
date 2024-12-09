package com.plasstech.lang.c.parser;

public record FunctionDef(String name, Statement body) implements AstNode {
  @Override
  public <R> R accept(AstNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
