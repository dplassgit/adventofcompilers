package com.plasstech.lang.c.parser;

import java.util.List;
import java.util.Optional;

import com.plasstech.lang.c.typecheck.FunType;

/**
 * Declaration of a function, either internal or external.
 */
public record FunDecl(String name, FunType funType, List<String> params, Optional<Block> body,
    Optional<StorageClass> storageClass) implements Declaration {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
