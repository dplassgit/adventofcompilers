package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.typecheck.Type;

/**
 * Represents a value: a constant or a variable.
 */
interface TackyVal {
  interface Visitor<R> {
    R visit(TackyVar tackyVar);

    R visit(TackyConstant tackyInt);
  }

  Type type();

  <R> R accept(Visitor<R> visitor);
}
