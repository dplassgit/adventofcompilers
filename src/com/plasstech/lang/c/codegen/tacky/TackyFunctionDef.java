package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

public record TackyFunctionDef(String identifier, List<TackyInstruction> instruction)
    implements TackyNode {
  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
