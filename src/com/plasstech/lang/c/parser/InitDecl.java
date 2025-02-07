package com.plasstech.lang.c.parser;

public record InitDecl(VarDecl decl) implements ForInit {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visit(this);
  }
}
