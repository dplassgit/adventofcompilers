package com.plasstech.lang.c.codegen;

import java.util.List;

public record AsmProgram(List<AsmTopLevel> topLevelNodes) implements AsmNode {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
