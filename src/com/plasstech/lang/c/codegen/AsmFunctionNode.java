package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmFunctionNode(String name, List<Instruction> instructions) implements AsmNode {
  @Override
  public <R> R accept(AsmNodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
