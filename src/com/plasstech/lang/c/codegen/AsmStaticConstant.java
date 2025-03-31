package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.typecheck.StaticInit;

/** Represents a float point constant in the "Assembly Generation" step. Page 324 */
public record AsmStaticConstant(String name, int alignment, StaticInit init)
    implements AsmTopLevel {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
