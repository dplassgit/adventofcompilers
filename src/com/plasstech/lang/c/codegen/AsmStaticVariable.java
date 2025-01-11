package com.plasstech.lang.c.codegen;

public record AsmStaticVariable(String name, boolean global, int initialValue)
    implements AsmTopLevel {

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
