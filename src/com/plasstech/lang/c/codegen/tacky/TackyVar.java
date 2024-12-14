package com.plasstech.lang.c.codegen.tacky;

/**
 * A variable in the Tacky IR.
 */
record TackyVar(String identifier) implements TackyVal {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
