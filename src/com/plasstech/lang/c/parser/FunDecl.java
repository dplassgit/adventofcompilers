package com.plasstech.lang.c.parser;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

/**
 * Declaration of a function, either internal or external.
 */
public record FunDecl(String name, List<String> params, Optional<Block> body)
    implements Declaration {

  public FunDecl(String name, Block body) {
    this(name, ImmutableList.of(), Optional.of(body));
  }

  public FunDecl(String name, List<String> params) {
    this(name, params, Optional.empty());
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
