package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.typecheck.StaticInit;

/** Represents a static variable in the "Assembly Generation" step. Page 235 */
public record AsmStaticVariable(String name, boolean global, int alignment, StaticInit init)
    implements AsmTopLevel {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
