package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmProgramNode(List<AsmFunctionNode> functions) implements AsmNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
