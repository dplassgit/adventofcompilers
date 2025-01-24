package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.typecheck.Type;

/**
 * This can/should be used for char, short, int, long...
 */
record TackyConstant(Type type, long val) implements TackyVal {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
