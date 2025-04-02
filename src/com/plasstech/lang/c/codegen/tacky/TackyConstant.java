package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.typecheck.Type;

/**
 * This can/should be used for char, short, int, long...
 */
record TackyConstant<T extends Number>(Type type, T val) implements TackyVal {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
