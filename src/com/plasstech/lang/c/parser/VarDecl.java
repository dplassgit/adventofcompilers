package com.plasstech.lang.c.parser;

import java.util.Optional;

/**
 * Declaration of a variable, with an optional initial expression.
 */
public record VarDecl(String name, Optional<Exp> init, Optional<StorageClass> storageClass)
    implements Declaration {
  public VarDecl(String name, Optional<StorageClass> storageClass) {
    this(name, Optional.empty(), storageClass);
  }

  public VarDecl(String name, Exp init, Optional<StorageClass> storageClass) {
    this(name, Optional.of(init), storageClass);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
