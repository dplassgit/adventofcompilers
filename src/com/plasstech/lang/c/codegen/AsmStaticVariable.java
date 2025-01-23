package com.plasstech.lang.c.codegen;

/** Represents a static variable in the "Assembly Generation" step. Page 235 */
public record AsmStaticVariable(String name, boolean global, long initialValue)
    implements AsmTopLevel {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
