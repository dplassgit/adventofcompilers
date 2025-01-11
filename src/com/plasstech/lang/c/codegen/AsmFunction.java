package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmFunction(String name, boolean global, List<Instruction> instructions)
    implements AsmTopLevel {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
