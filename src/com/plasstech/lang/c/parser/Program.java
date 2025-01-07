package com.plasstech.lang.c.parser;

import java.util.List;

public record Program(List<Declaration> declarations) implements AstNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  public List<FunDecl> funDecls() {
    return declarations.stream().filter(d -> d instanceof FunDecl).map(fd -> (FunDecl) fd).toList();
  }
}
