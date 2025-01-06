package com.plasstech.lang.c.parser;

import java.util.List;
import java.util.Optional;

/**
 * Declaration of a function, either internal or external.
 */
public record FunDecl(String name, List<String> params, Optional<Block> body,
    StorageClass storageClass)
    implements Declaration {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
