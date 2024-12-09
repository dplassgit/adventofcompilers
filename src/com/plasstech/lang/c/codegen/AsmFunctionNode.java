package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmFunctionNode(String name, List<Instruction> instructions) implements AsmNode {
  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }
}
