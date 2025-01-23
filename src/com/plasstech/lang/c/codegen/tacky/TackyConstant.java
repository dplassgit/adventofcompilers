package com.plasstech.lang.c.codegen.tacky;

/**
 * This can/should be used for char, short, int, long...
 */
record TackyConstant(long val) implements TackyVal {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
