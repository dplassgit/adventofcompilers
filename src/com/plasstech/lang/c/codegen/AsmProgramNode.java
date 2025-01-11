package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmProgramNode(List<AsmTopLevelNode> topLevelNodes) implements AsmNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
