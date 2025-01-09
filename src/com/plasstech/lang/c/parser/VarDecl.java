package com.plasstech.lang.c.parser;

import java.util.Optional;

/**
 * Declaration of a variable, with an optional initial expression.
 */
public record VarDecl(String identifier, Optional<Exp> init, Optional<StorageClass> storageClass)
    implements Declaration {
  public VarDecl(String identifier, Optional<StorageClass> storageClass) {
    this(identifier, Optional.empty(), storageClass);
  }

  public VarDecl(String identifier, Exp init, Optional<StorageClass> storageClass) {
    this(identifier, Optional.of(init), storageClass);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
