package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmFunctionNode(String name, boolean global, List<Instruction> instructions)
    implements AsmTopLevelNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
