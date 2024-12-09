package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

public class TackyFunctionDef extends TackyNode {
  private final String identifier;
  private final List<TackyInstruction> instructions;

  public TackyFunctionDef(String identifier, List<TackyInstruction> instructions) {
    this.identifier = identifier;
    this.instructions = instructions;
  }

  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }

  public String identifier() {
    return identifier;
  }

  public List<TackyInstruction> instructions() {
    return instructions;
  }
}
