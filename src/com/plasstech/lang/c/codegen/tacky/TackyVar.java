package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.typecheck.Type;

/**
 * A variable in the Tacky IR.
 */
record TackyVar(String identifier, Type type) implements TackyVal {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
