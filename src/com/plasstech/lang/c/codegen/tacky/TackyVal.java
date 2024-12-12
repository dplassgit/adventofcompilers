package com.plasstech.lang.c.codegen.tacky;

/**
 * Represents a value: a constant or a variable.
 */
public interface TackyVal {
  public interface Visitor<R> {
    R visit(TackyVar tackyVar);

    R visit(TackyIntConstant tackyInt);
  }

  <R> R accept(Visitor<R> visitor);
}
