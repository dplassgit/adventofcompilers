package com.plasstech.lang.c.parser;

import java.util.Optional;

import com.plasstech.lang.c.typecheck.Type;

/**
 * Declaration of a variable, with an optional initial expression.
 */
public record VarDecl(String name, Type type, Optional<Exp> init,
    Optional<StorageClass> storageClass)
    implements Declaration {
  public VarDecl(String name, Type type, Optional<StorageClass> storageClass) {
    this(name, type, Optional.empty(), storageClass);
  }

  public VarDecl(String name, Type type, Exp init, Optional<StorageClass> storageClass) {
    this(name, type, Optional.of(init), storageClass);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
