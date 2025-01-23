package com.plasstech.lang.c.codegen.tacky;

/**
 * Represents a value: a constant or a variable.
 */
interface TackyVal {
  interface Visitor<R> {
    R visit(TackyVar tackyVar);

    R visit(TackyConstant tackyInt);
  }

  <R> R accept(Visitor<R> visitor);
}
