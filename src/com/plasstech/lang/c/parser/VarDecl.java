package com.plasstech.lang.c.parser;

import java.util.Optional;

/**
 * Declaration of a variable, with an optional initial expression.
 */
public record VarDecl(String identifier, Optional<Exp> init, StorageClass storageClass)
    implements Declaration {
  public VarDecl(String identifier, StorageClass storageClass) {
    this(identifier, Optional.empty(), storageClass);
  }

  public VarDecl(String identifier, Exp init, StorageClass storageClass) {
    this(identifier, Optional.of(init), storageClass);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
